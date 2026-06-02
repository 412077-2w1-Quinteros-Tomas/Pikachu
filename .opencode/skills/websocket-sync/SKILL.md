---
name: websocket-sync
description: Implements WebSocket communication for real-time game state synchronization in the Pokemon TCG project. Use when creating or editing files related to WebSocket, STOMP, real-time communication, game actions, state updates, or the GameWebSocketHandler. Triggers on WebSocket, STOMP, real-time, socket, ws, game action, state sync, lobby, match connection.
---

# WebSocket Sync Skill - Pokemon TCG

You are implementing the real-time communication layer for a Pokemon Trading Card Game. The backend is the ONLY source of truth during gameplay. All game actions go through WebSocket, NOT REST.

## Architecture

### Communication Model

```
CLIENT (Angular)                          SERVER (Spring Boot)
    │                                          │
    │  ─── HTTP REST ──────────────────────►   │  Cards, Decks, Match CRUD
    │                                          │
    │  ─── WebSocket Connect ─────────────►   │  /ws/game
    │                                          │
    │  ─── GameActionMessage ──────────────►   │  All in-game actions
    │                                          │
    │  ◄── GameStateUpdateMessage ──────────    │  State sync, events, errors
    │                                          │
```

### Key Constraint

**During gameplay (after match SETUP phase starts), NO REST API calls are allowed.** All actions go through WebSocket → GameEngine → State persistence → Broadcast.

The lobby (creating/joining matches) and deck builder still use REST. Only the live game uses WebSocket.

## Server-Side Implementation

### WebSocketConfig

Located at: `configs/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/game")
                .setAllowedOriginPatterns("http://localhost:4200")
                .withSockJS();
    }
}
```

### GameWebSocketHandler

Located at: `websocket/GameWebSocketHandler.java`

Responsible for:
1. Managing connections per match
2. Routing incoming `GameActionMessage` to `GameEngine.processAction()`
3. Broadcasting `GameStateUpdateMessage` to both players in a match
4. Handling reconnection (players receive full state on reconnect)

```java
@Component
public class GameWebSocketHandler {

    private final GameEngine gameEngine;
    private final ObjectMapper objectMapper;

    // Key: matchId, Value: Set of session IDs
    private final Map<String, Set<String>> matchSessions = new ConcurrentHashMap<>();

    // Key: sessionId, Value: matchId
    private final Map<String, String> sessionMatch = new ConcurrentHashMap<>();

    // Key: sessionId, Value: playerId
    private final Map<String, String> sessionPlayer = new ConcurrentHashMap<>();

    public void handleConnect(String sessionId, String matchId, String playerId) { ... }
    public void handleDisconnect(String sessionId) { ... }
    public void handleAction(String sessionId, GameActionMessage action) { ... }
    public void broadcastToMatch(String matchId, GameStateUpdateMessage message) { ... }
    public void sendToPlayer(String sessionId, GameStateUpdateMessage message) { ... }
}
```

### Message Types

#### Client → Server: `GameActionMessage`

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameActionMessage {
    private String matchId;
    private GameActionType type;
    private Map<String, Object> payload;
    private String timestamp;
}
```

Action types:

| Type | Description | Payload |
|---|---|---|
| `DRAW_CARD` | Draw 1 card from deck | `{}` |
| `PLACE_POKEMON` | Place Basic Pokemon on bench | `{ "cardId": "uuid", "zone": "BENCH" }` |
| `SET_ACTIVE` | Set active Pokemon (setup/KO) | `{ "cardId": "uuid" }` |
| `ATTACH_ENERGY` | Attach energy to Pokemon | `{ "energyCardId": "uuid", "targetPokemonId": "uuid" }` |
| `PLAY_TRAINER` | Play trainer card | `{ "cardId": "uuid" }` |
| `EVOLVE` | Evolve Pokemon | `{ "basePokemonId": "uuid", "evolutionCardId": "uuid" }` |
| `RETREAT` | Retreat active Pokemon | `{ "newActivePokemonId": "uuid" }` |
| `ATTACK` | Declare attack | `{ "attackName": "Flamethrower" }` |
| `USE_ABILITY` | Use Pokemon ability | `{ "abilityName": "Fire Breath" }` |
| `READY` | Player ready (setup phase) | `{}` |
| `SURRENDER` | Forfeit match | `{}` |

#### Server → Client: `GameStateUpdateMessage`

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameStateUpdateMessage {
    private String type;  // STATE_UPDATE, EVENT, ERROR, NOTIFICATION
    private MatchSnapshot state;
    private GameEvent event;
    private String error;
    private String notification;
}
```

Message types:
- `STATE_UPDATE`: Full state sync after an action
- `EVENT`: Game event notification (KO, prize taken, etc.)
- `ERROR`: Action was invalid with reason
- `NOTIFICATION`: Info message (turn changed, phase changed, etc.)

### Security Rules for State Broadcasts

When broadcasting `MatchSnapshot`:
1. **Opponent's hand is NEVER sent.** Only show `handSize` for the opponent.
2. **Deck is never revealed.** Only show `deckCount`.
3. **Prize cards are face-down.** Only show `prizeCardsRemaining` count.
4. **Opponent cannot see face-down cards.**

This means `GameWebSocketHandler` must send DIFFERENT state snapshots to each player:

```java
public void broadcastToMatch(String matchId, GameStateUpdateMessage baseMessage) {
    MatchSnapshot fullSnapshot = baseMessage.getState();

    // Create player-specific views
    MatchSnapshot p1View = sanitizeForPlayer(fullSnapshot, player1Id);
    MatchSnapshot p2View = sanitizeForPlayer(fullSnapshot, player2Id);

    sendToPlayer(player1SessionId, new GameStateUpdateMessage(..., p1View, ...));
    sendToPlayer(player2SessionId, new GameStateUpdateMessage(..., p2View, ...));
}

private MatchSnapshot sanitizeForPlayer(MatchSnapshot snapshot, String playerId) {
    // Hide opponent's hand (show only count)
    // Hide deck order (show only count)
    // Hide prize cards content (show only count)
    // Keep own hand visible
    // Keep own prize cards visible when taken
}
```

## Client-Side Implementation

### WebsocketService

```typescript
@Injectable({ providedIn: 'root' })
export class WebsocketService {
  private readonly connectionStatus = signal<'connected' | 'disconnected' | 'connecting'>('disconnected');
  private stompClient: Client | null = null;

  readonly status = computed(() => this.connectionStatus());

  connect(matchId: string, playerId: string): void {
    this.connectionStatus.set('connecting');
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws/game',
      reconnectDelay: 5000,
      onConnect: () => {
        this.connectionStatus.set('connected');
        this.stompClient?.subscribe(`/topic/match/${matchId}`, (message) => {
          this.handleMessage(JSON.parse(message.body));
        });
        this.stompClient?.publish({
          destination: `/app/match/${matchId}/connect`,
          body: JSON.stringify({ playerId })
        });
      },
      onDisconnect: () => this.connectionStatus.set('disconnected')
    });
    this.stompClient.activate();
  }

  sendAction(action: GameActionMessage): void {
    this.stompClient?.publish({
      destination: `/app/match/${action.matchId}/action`,
      body: JSON.stringify(action)
    });
  }

  disconnect(): void { ... }
}
```

### GameWebsocketService

```typescript
@Injectable({ providedIn: 'root' })
export class GameWebsocketService {
  private readonly websocket = inject(WebsocketService);
  private readonly gameStateService = inject(GameStateService);

  connectToMatch(matchId: string, playerId: string): void {
    this.websocket.connect(matchId, playerId);
  }

  sendAction(type: GameActionType, payload: Record<string, unknown> = {}): void {
    const action: GameActionMessage = {
      matchId: this.matchId(),
      type,
      payload,
      timestamp: new Date().toISOString()
    };
    this.websocket.sendAction(action);
  }

  private handleStateUpdate(message: GameStateUpdateMessage): void {
    switch (message.type) {
      case 'STATE_UPDATE':
        this.gameStateService.updateState(message.state!);
        break;
      case 'EVENT':
        this.gameStateService.addEvent(message.event!);
        break;
      case 'ERROR':
        this.gameStateService.addNotification({ type: 'error', text: message.error! });
        break;
      case 'NOTIFICATION':
        this.gameStateService.addNotification({ type: 'info', text: message.notification! });
        break;
    }
  }
}
```

## Reconnection Flow

1. Client disconnects (network issue, tab close, etc.)
2. Server keeps match state (persisted after every action)
3. Client reconnects via WebSocket with same matchId
4. Server sends full `MatchSnapshot` to reconnecting player
5. Client rebuilds UI from the snapshot
6. Any actions the player "missed" are already in the snapshot

## STOMP Endpoints

| Endpoint | Direction | Description |
|---|---|---|
| `/ws/game` | Client connects | WebSocket handshake endpoint |
| `/app/match/{matchId}/connect` | Client → Server | Player connects to match |
| `/app/match/{matchId}/action` | Client → Server | Player sends game action |
| `/topic/match/{matchId}` | Server → Client | Broadcast for match updates |

## Checklist

When implementing WebSocket features:
- [ ] `WebSocketConfig` uses STOMP withSockJS
- [ ] CORS allows `http://localhost:4200`
- [ ] Game actions are routed through `GameEngine.processAction()`
- [ ] State is sanitized per-player before broadcast (no opponent hand, no deck order)
- [ ] Reconnection sends full state snapshot
- [ ] `GameStatePersistenceService` called after every action
- [ ] Frontend uses `@stomp/stompjs` or similar STOMP client
- [ ] Connection status exposed as signal
- [ ] Error messages are user-friendly
- [ ] Match sessions cleaned up on disconnect