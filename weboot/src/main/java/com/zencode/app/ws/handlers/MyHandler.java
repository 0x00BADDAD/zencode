package com.zencode.app.ws.handlers;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MyHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
            session,
            10_000,  // send timeout in ms
            1024     // buffer size in bytes
        );
        sessions.put(session.getId(), safeSession);
    }

    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            try {
                session.sendMessage(textMessage); // no need to synchronize
            } catch (IOException e) {
                // log and remove dead sessions
            }
        }
    }
}

