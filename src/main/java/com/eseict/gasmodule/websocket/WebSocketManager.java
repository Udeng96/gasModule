package com.eseict.gasmodule.websocket;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WebSocketManager {


    private final AtomicBoolean connected = new AtomicBoolean(false);

    public boolean isConnected() { return connected.get(); }
    public void setConnected(boolean connected) { this.connected.set(connected); }

}
