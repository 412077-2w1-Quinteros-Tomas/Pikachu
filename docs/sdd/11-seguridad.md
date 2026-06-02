# 11 - Seguridad

## Principios de Seguridad (RNF-05)

### 1. Backend Valida TODAS las Acciones

Cada acción que recibe el servidor a través de WebSocket se valida antes de aplicarse:

```java
public class GameWebSocketHandler {
    void handleAction(GameActionMessage action) {
        ruleValidator.validateAction(action, board, turn);
        // Solo si pasa la validación, se aplica la acción
        gameEngine.processAction(action);
    }
}
```

**Nunca se confía en el cliente.** Todas las validaciones se hacen en el servidor:

- ¿Es el turno del jugador que envía la acción?
- ¿La acción es válida en la fase actual?
- ¿El jugador tiene los recursos necesarios (energía, cartas)?
- ¿La acción viola alguna regla del juego?

### 2. Mano del Oponente Nunca se Envía al Cliente

El `MatchSnapshot` que se envía a los clientes filtra la mano del oponente:

```java
public class MatchSnapshot {
    public PlayerStateDTO getPlayerState(String requestingPlayerId) {
        if (requestingPlayerId.equals(player1Id)) {
            return new PlayerStateDTO(player1Board, player2Board.getHandCount());
        } else {
            return new PlayerStateDTO(player2Board, player1Board.getHandCount());
        }
    }
}
```

El cliente solo recibe:
- Su propia mano (cartas completas)
- La cantidad de cartas en la mano del oponente (no las cartas)

### 3. Orden del Mazo y Cartas de Premio Ocultos

El servidor mantiene el orden real del mazo y las cartas de premio. El cliente solo recibe:

- Cantidad de cartas restantes en el mazo
- Cantidad de cartas de premio restantes
- Las cartas de premio se revelan solo cuando se toman

### 4. Sin Llamadas API Directas Durante el Juego

Durante una partida activa:

- Todas las acciones van exclusivamente por WebSocket → GameEngine
- Los endpoints REST de cartas, mazos y matches están disponibles fuera del juego
- El GameEngine es el único que puede modificar el estado de la partida

### 5. Validación de Inputs

Todos los DTOs usan Bean Validation:

```java
public class CreateDeckDTO {
    @NotBlank(message = "El nombre del mazo es obligatorio")
    String name;

    @NotEmpty(message = "El mazo debe tener cartas")
    @Size(min = 60, max = 60, message = "El mazo debe tener exactamente 60 cartas")
    List<DeckCardEntryDTO> cards;
}
```

### 6. API Key Externa

La API key de pokemontcg.io se maneja mediante variable de entorno:

```properties
pokemon.api.key=${POKEMON_TCG_API_KEY}
```

**Nunca se commitea la API key al repositorio.**

Archivos ignorados en `.gitignore`:

```
# Secrets
.env
.env.local
*.env
**/application-local.properties
```

## Resumen de Reglas de Seguridad

| Regla | Implementación |
|---|---|
| Validación de acciones | `RuleValidator` en cada acción WebSocket |
| Mano del oponente oculta | `MatchSnapshot` filtra cartas de mano |
| Orden de mazo/premios oculto | Solo se envían cantidades al cliente |
| Sin REST durante juego | Todas las acciones van por WebSocket |
| Validación de inputs | `@Valid` + Bean Validation en DTOs |
| API key segura | Variable de entorno, no en código |
| CORS configurado | `CorsConfig` restringe orígenes |
