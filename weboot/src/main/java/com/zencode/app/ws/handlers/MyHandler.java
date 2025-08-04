package com.zencode.app.ws.handlers;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zencode.app.ws.handlers.beans.TrackMetadataBean;

public class MyHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(MyHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.debug("got a connection to the web socket endpoint!");
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
            session,
            10_000,  // send timeout in ms
            1024     // buffer size in bytes
        );
        sessions.put(session.getId(), safeSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove session
        sessions.remove(session.getId());
        logger.debug("Session removed: " + session.getId());
    }

    public void broadcast(TrackMetadataBean message) {
        logger.debug("broadcasting message to clients with count " + sessions.size());

        for (WebSocketSession session : sessions.values()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                if (session.isOpen()){
                    session.sendMessage(new TextMessage(json)); // no need to synchronize
                }else{
                    logger.debug("Session is stale!!!");
                }
            } catch (IOException e) {
                // log and remove dead sessions
            }
        }
    }
}

