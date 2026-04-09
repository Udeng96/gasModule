package com.eseict.gasmodule.cache;

import com.eseict.gasmodule.repository.postgres.GasLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class GasCache {

    private final AtomicInteger lastEventId = new AtomicInteger(0);
    private final GasLogRepository gasLogRepository;

    /**
     * PostgreSQL에 마지막으로 저장된 MSSQL idx로 초기화.
     * → 서버 재시작 시 밀린 이벤트를 자동으로 따라잡음 (catch-up)
     */
    @PostConstruct
    public void init() {
        log.info("[가스] 최신 이벤트 ID 초기화 시작 (PostgreSQL 기반)");
        int maxIdx = gasLogRepository.findMaxMssqlIdx();
        updateLastEventId(maxIdx);
        log.info("[가스] 최신 이벤트 ID 초기화 완료: {}", lastEventId.get());
    }

    public int getLastEventId() {
        return lastEventId.get();
    }

    public void updateLastEventId(int id) {
        lastEventId.set(id);
    }
}
