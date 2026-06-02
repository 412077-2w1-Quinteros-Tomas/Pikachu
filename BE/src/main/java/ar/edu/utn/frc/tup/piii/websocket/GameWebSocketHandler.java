package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.websocket.messages.GameActionMessage;
import ar.edu.utn.frc.tup.piii.websocket.messages.GameStateUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    /** matchId → connected sessions */
    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String matchId = getQueryParam(session, "matchId");
        String playerId = getQueryParam(session, "playerId");

        if (matchId == null || matchId.isBlank()) {
            session.close(CloseStatus.BAD_DATA.withReason("matchId required"));
            return;
        }

        rooms.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(session);
        session.getAttributes().put("matchId", matchId);
        session.getAttributes().put("playerId", playerId);

        broadcast(matchId, new GameStateUpdateMessage("PLAYER_CONNECTED", matchId, Map.of(
                "playerId", playerId != null ? playerId : "unknown",
                "playerCount", rooms.get(matchId).size())));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        GameActionMessage action = objectMapper.readValue(message.getPayload(), GameActionMessage.class);
        String matchId = (String) session.getAttributes().get("matchId");

        broadcast(matchId, new GameStateUpdateMessage("STATE_UPDATE", matchId, Map.of(
                "actionType", action.getActionType() != null ? action.getActionType() : "",
                "playerId", action.getPlayerId() != null ? action.getPlayerId() : "",
                "payload", action.getPayload() != null ? action.getPayload() : Map.of())));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String matchId = (String) session.getAttributes().get("matchId");
        String playerId = (String) session.getAttributes().get("playerId");

        if (matchId != null) {
            List<WebSocketSession> room = rooms.get(matchId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    rooms.remove(matchId);
                } else {
                    try {
                        broadcast(matchId, new GameStateUpdateMessage("PLAYER_DISCONNECTED", matchId,
                                Map.of("playerId", playerId != null ? playerId : "unknown")));
                    } catch (IOException ignored) { }
                }
            }
        }
    }

    public void broadcast(String matchId, GameStateUpdateMessage message) throws IOException {
        List<WebSocketSession> room = rooms.get(matchId);
        if (room == null || room.isEmpty()) return;
        String json = objectMapper.writeValueAsString(message);
        for (WebSocketSession s : room) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    public int getRoomSize(String matchId) {
        List<WebSocketSession> room = rooms.get(matchId);
        return room != null ? room.size() : 0;
    }

    private String getQueryParam(WebSocketSession session, String param) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) return kv[1];
        }
        return null;
    }
}
