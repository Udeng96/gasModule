package com.eseict.gasmodule.schedule;

import com.eseict.gasmodule.serv.GasEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GasPolling {

    private final GasEventService gasEventService;
    private static final int KEEP_ALIVE_INTERVAL = 60;
    private int pollingCnt = 0;

    /**
     * 1초마다 MSSQL 폴링 (WebSocket 연결 여부와 무관하게 항상 실행)
     * - 데이터 수집/로그 저장은 항상 수행
     * - WebSocket 알림은 연결 시에만 전송 (WebSocket 내부에서 판단)
     */
    @Scheduled(fixedRate = 1000)
    public void pollNewEvents() {
        gasEventService.subscribe();

        pollingCnt++;
        if (pollingCnt >= KEEP_ALIVE_INTERVAL) {
            pollingCnt = 0;
            log.info("Gas Polling Keep Alive");
        }
    }
}
