package com.eseict.gasmodule.serv;

import com.eseict.gasmodule.cache.GasCache;
import com.eseict.gasmodule.data.constant.LevelEnum;
import com.eseict.gasmodule.data.constant.StateEnum;
import com.eseict.gasmodule.data.mssql.dto.GasRawInfo;
import com.eseict.gasmodule.data.postgres.domain.GasEvent;
import com.eseict.gasmodule.data.postgres.domain.GasLog;
import com.eseict.gasmodule.data.postgres.domain.GasSensor;
import com.eseict.gasmodule.repository.postgres.GasEventRepository;
import com.eseict.gasmodule.repository.postgres.GasLogRepository;
import com.eseict.gasmodule.repository.postgres.GasSensorRepository;
import com.eseict.gasmodule.util.Util;
import com.eseict.gasmodule.websocket.WebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GasEventService {

    private static final Logger eventLog = LoggerFactory.getLogger("EVENT");

    private final Util util;
    private final GasCache gasCache;
    private final GasRawService gasRawService;
    private final GasSensorRepository gasSensorRepository;
    private final GasLogRepository gasLogRepository;
    private final GasEventRepository gasEventRepository;
    private final WebSocket webSocket;

    // ==================== 폴링 진입점 ====================

    public void subscribe() {
        int cacheIdx = gasCache.getLastEventId();
        List<GasRawInfo> newRawEvents = gasRawService.getNewRawEvents(cacheIdx);

        for (GasRawInfo rawEvent : newRawEvents) {
            try {
                if (util.isGasEvent(rawEvent.getState())) {
                    handleEvent(rawEvent);
                }
                gasCache.updateLastEventId(rawEvent.getIdx());
            } catch (Exception e) {
                log.error("가스 이벤트 처리 실패. idx={}", rawEvent.getIdx(), e);
                break;
            }
        }
    }

    // ==================== 개별 이벤트 처리 ====================

    public void handleEvent(GasRawInfo rawEvent) {
        String sensorId = normalizeSensorId(rawEvent.getDetector());
        OffsetDateTime detectedAt = rawEvent.getDate()
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();

        StateEnum rawState = StateEnum.fromState(rawEvent.getState());
        LevelEnum rawLevel = LevelEnum.fromState(rawEvent.getState());

        if (rawState == StateEnum.INVALID || rawLevel == LevelEnum.LEVEL_INVALID) {
            log.warn("유효하지 않은 가스 이벤트. state={}", rawEvent.getState());
            return;
        }

        // 센서 → 건물 매핑 조회 (새 스키마: gas_sensor 테이블)
        Optional<GasSensor> sensorOpt = gasSensorRepository.findBySensorId(sensorId);
        if (!sensorOpt.isPresent()) {
            log.warn("센서 정보 없음. sensorId={}", sensorId);
            return;
        }
        GasSensor sensor = sensorOpt.get();
        Long buildingId = sensor.getBuildingId();

        // 센서의 최근 로그에서 현재 상태 파악
        LevelEnum logLevel = LevelEnum.LEVEL0;
        StateEnum logState = StateEnum.CLEAR;

        Optional<GasLog> lastLogOpt = gasLogRepository.findTopByDetectorCdOrderByDetectedAtDesc(sensorId);
        if (lastLogOpt.isPresent()) {
            GasLog lastLog = lastLogOpt.get();
            logLevel = LevelEnum.fromCode(lastLog.getLevel());
            logState = StateEnum.fromCode(lastLog.getLevelStatus());
        }

        // 상태 전이 규칙 적용
        LevelEnum newLevel = resolveNextLevel(logLevel, logState, rawLevel);
        StateEnum newState = resolveNextState(logLevel, logState, rawLevel);

        if (newLevel == LevelEnum.LEVEL_INVALID || newState == StateEnum.INVALID) {
            log.warn("유효하지 않은 전이. sensor={}, logLevel={}, logState={}, rawLevel={}",
                    sensorId, logLevel, logState, rawLevel);
            return;
        }

        // 이벤트 처리 + 로그 저장
        Long gasEventId = handleGasEvent(sensorId, buildingId, newLevel, newState, detectedAt);
        if (gasEventId != null) {
            saveLog(gasEventId, sensorId, buildingId, newLevel, newState, detectedAt, rawEvent.getIdx());
        }
    }

    // ==================== 이벤트 처리 (센서 단위) ====================

    private Long handleGasEvent(String sensorId, Long buildingId,
                                LevelEnum newLevel, StateEnum newState,
                                OffsetDateTime detectedAt) {
        Optional<GasEvent> activeEvent = gasEventRepository.findByDetectorCdAndClearedAtIsNull(sensorId);

        if (newState == StateEnum.OCCUR) {
            return handleOccur(activeEvent, sensorId, buildingId, newLevel, detectedAt);
        } else {
            return handleClear(activeEvent, sensorId, detectedAt);
        }
    }

    private Long handleOccur(Optional<GasEvent> activeEvent, String sensorId,
                             Long buildingId, LevelEnum newLevel, OffsetDateTime detectedAt) {
        if (!activeEvent.isPresent()) {
            // 활성 이벤트 없음 → 신규 발생
            GasEvent event = gasEventRepository.save(GasEvent.builder()
                    .detectorCd(sensorId)
                    .occurredAt(detectedAt)
                    .build());
            eventLog.info("[sensor:{}] 가스 이벤트 발생 (L{})", sensorId, newLevel.getCode());
            notifyWeb(event);
            return event.getId();
        }

        // 활성 이벤트 있음 → 에스컬레이션
        GasEvent event = activeEvent.get();
        if (newLevel.getCode() > LevelEnum.LEVEL1.getCode()) {
            eventLog.info("[sensor:{}] 가스 이벤트 에스컬레이션 → L{}", sensorId, newLevel.getCode());
            notifyWeb(event);
        }
        return event.getId();
    }

    private Long handleClear(Optional<GasEvent> activeEvent, String sensorId,
                             OffsetDateTime detectedAt) {
        if (!activeEvent.isPresent()) {
            return null;
        }

        GasEvent event = activeEvent.get();
        event.setClearedAt(detectedAt);
        gasEventRepository.save(event);
        eventLog.info("[sensor:{}] 가스 이벤트 해제", sensorId);
        notifyWeb(event);
        return event.getId();
    }

    // ==================== 상태 전이 규칙 ====================
    //
    // 시나리오 A: L1발생 → L1해제 → 끝
    // 시나리오 B: L1발생 → L1해제 → L2발생 → L2해제 → L3발생 → L3해제 → 끝
    //
    // | logLevel | logState | rawLevel | → newLevel | → newState |
    // |----------|----------|----------|------------|------------|
    // | L0       | *        | L1       | L1         | OCCUR      |
    // | L1       | OCCUR    | L1       | L1         | CLEAR      |
    // | L1       | CLEAR    | L1       | L1         | OCCUR      |
    // | L1       | CLEAR    | L2       | L2         | OCCUR      |
    // | L2       | OCCUR    | L2       | L2         | CLEAR      |
    // | L2       | CLEAR    | L1       | L3         | OCCUR      |
    // | L3       | OCCUR    | L1       | L3         | CLEAR      |
    // | L3       | CLEAR    | L1       | L1         | OCCUR      | ← 새 사이클

    LevelEnum resolveNextLevel(LevelEnum logLevel, StateEnum logState, LevelEnum rawLevel) {
        if (logLevel == LevelEnum.LEVEL0) {
            return rawLevel == LevelEnum.LEVEL1 ? LevelEnum.LEVEL1 : LevelEnum.LEVEL_INVALID;
        }

        if (logLevel == LevelEnum.LEVEL1) {
            if (logState == StateEnum.OCCUR) {
                return rawLevel == LevelEnum.LEVEL1 ? LevelEnum.LEVEL1 : LevelEnum.LEVEL_INVALID;
            } else {
                if (rawLevel == LevelEnum.LEVEL1) return LevelEnum.LEVEL1;
                if (rawLevel == LevelEnum.LEVEL2) return LevelEnum.LEVEL2;
                return LevelEnum.LEVEL_INVALID;
            }
        }

        if (logLevel == LevelEnum.LEVEL2) {
            if (logState == StateEnum.OCCUR) {
                return rawLevel == LevelEnum.LEVEL2 ? LevelEnum.LEVEL2 : LevelEnum.LEVEL_INVALID;
            } else {
                return rawLevel == LevelEnum.LEVEL1 ? LevelEnum.LEVEL3 : LevelEnum.LEVEL_INVALID;
            }
        }

        if (logLevel == LevelEnum.LEVEL3) {
            if (logState == StateEnum.OCCUR) {
                return rawLevel == LevelEnum.LEVEL1 ? LevelEnum.LEVEL3 : LevelEnum.LEVEL_INVALID;
            } else {
                return rawLevel == LevelEnum.LEVEL1 ? LevelEnum.LEVEL1 : LevelEnum.LEVEL_INVALID;
            }
        }

        return LevelEnum.LEVEL_INVALID;
    }

    StateEnum resolveNextState(LevelEnum logLevel, StateEnum logState, LevelEnum rawLevel) {
        LevelEnum nextLevel = resolveNextLevel(logLevel, logState, rawLevel);
        if (nextLevel == LevelEnum.LEVEL_INVALID) {
            return StateEnum.INVALID;
        }

        if (logLevel == LevelEnum.LEVEL0) {
            return StateEnum.OCCUR;
        }

        // 같은 레벨 유지 → 상태 토글 (발생↔해제)
        if (logLevel == nextLevel) {
            return logState == StateEnum.OCCUR ? StateEnum.CLEAR : StateEnum.OCCUR;
        }

        // 레벨 변경 → 항상 발생 (에스컬레이션 또는 새 사이클)
        return StateEnum.OCCUR;
    }

    // ==================== 데이터 저장 ====================

    private void saveLog(Long gasEventId, String sensorId, Long buildingId,
                         LevelEnum level, StateEnum state,
                         OffsetDateTime detectedAt, int mssqlIdx) {
        gasLogRepository.save(GasLog.builder()
                .gasEventId(gasEventId)
                .detectorCd(sensorId)
                .buildingId(buildingId)
                .level(level.getCode())
                .levelStatus(state.getCode())
                .detectedAt(detectedAt)
                .mssqlIdx(mssqlIdx)
                .build());
    }

    // ==================== 유틸리티 ====================

    private String normalizeSensorId(String sensorId) {
        return sensorId != null ? sensorId.trim().replaceAll("\\s+", "") : "";
    }

    private void notifyWeb(GasEvent event) {
        webSocket.sendToWeb(event);
    }
}
