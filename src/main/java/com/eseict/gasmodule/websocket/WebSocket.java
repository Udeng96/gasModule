package com.eseict.gasmodule.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocket {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketManager webSocketManager;

    public void sendToWeb(Object payload) {
        if (!webSocketManager.isConnected()) {
            log.debug("WebSocket 미연결 상태, 전송 생략");
            return;
        }
        messagingTemplate.convertAndSend("/event/gas", payload);
        log.info("가스 이벤트 웹으로 전송됨");
    }

}
