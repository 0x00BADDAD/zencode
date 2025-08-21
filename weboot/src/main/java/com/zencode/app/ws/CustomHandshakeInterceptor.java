package com.zencode.app.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;





public class CustomHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // extract query param, header, or cookie
        URI uri = request.getURI();
        String query = uri.getQuery(); // e.g., "session_id=..."

        if (query != null) {
            Map<String, String> queryParams = parseQueryParams(query);

            // Store them in session attributes (available in WebSocketSession)
            attributes.putAll(queryParams);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) { }

    private Map<String, String> parseQueryParams(String query) {

            Map<String, String> result = new HashMap<>();
            for (String param : query.split("&")) {
                String[] parts = param.split("=");
                if (parts.length == 2) {
                    result.put(parts[0], parts[1]);
                }
            }
            return result;
    }
}
