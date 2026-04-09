package com.eseict.gasmodule.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler {

    private final WebSocketManager connectionManager;

    // ✅ 클라이언트 연결 시
    @EventListener
    public void handleConnectEvent(SessionConnectEvent event) {
        connectionManager.setConnected(true);
        log.info("[GAS] WebSocket Client Connected: {}", event.getMessage().getHeaders());
    }

    // ✅ STOMP CONNECT 처리 완료 시
    @EventListener
    public void handleConnectedEvent(SessionConnectedEvent event) {
        log.info("[GAS] STOMP Connected: sessionId={}", event.getMessage().getHeaders().get("simpSessionId"));
    }

    // ✅ 클라이언트 구독 이벤트
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        log.info("[GAS] Client Subscribed: {}", event.getMessage().getHeaders().get("simpDestination"));
    }

    // ✅ 클라이언트 연결 끊김
    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        connectionManager.setConnected(false);
        log.warn("[GAS] WebSocket Disconnected: sessionId={}, closeStatus={}",
                event.getSessionId(), event.getCloseStatus());
    }
}
