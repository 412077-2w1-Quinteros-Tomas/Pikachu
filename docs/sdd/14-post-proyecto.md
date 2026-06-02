# 14 - Post-Proyecto

> **Nota:** Estas funcionalidades NO forman parte del plan de desarrollo inicial. Se implementan únicamente después de cumplir TODOS los requisitos obligatorios y alcanzar los 100 puntos.

## Funcionalidades Bonus (+15 puntos)

### 1. Deck Temático Seed (+5 puntos)

Mazo precargado temático con datos de ejemplo.

**Implementación:**
- Crear un `SeedDataLoader` que cargue mazos de ejemplo al iniciar la aplicación
- Mazos temáticos basados en tipos (Fire deck, Water deck, etc.)
- Asociar mazos seed a usuarios de ejemplo

**Archivos afectados:**
- Nuevo: `data/SeedDataLoader.java`
- `application.properties` (flag para habilitar seed)

**Estimación:** 2-3 días

### 2. Animaciones y Transiciones (+5 puntos)

Animaciones CSS para mejorar la experiencia visual.

**Implementación:**
- Animación de ataque (transición de carta atacante a defensor)
- Animación de daño (contador animado, flash rojo)
- Animación de evolución (transformación visual)
- Transiciones entre fases del turno
- Animación de cartas al robar/descartar
- Animación de condiciones especiales (quemado humeando, dormido con Zzz)

**Archivos afectados:**
- CSS de componentes de game (`features/game/components/*/`)
- Posible nueva librería de animaciones (Angular Animations)

**Estimación:** 3-5 días

### 3. Sistema de Ranking (+5 puntos)

Historial de partidas, estadísticas de jugadores, leaderboard.

**Implementación:**

**Backend:**
- Nueva entidad `PlayerEntity` con estadísticas
- Nueva entidad `MatchHistoryEntity` con resultados
- Endpoints REST para ranking y estadísticas
- Cálculo de ELO o sistema de puntos simple

**Frontend:**
- Página de ranking con leaderboard
- Perfil de jugador con estadísticas (partidas ganadas/perdidas, win rate)
- Historial de partidas

**Archivos afectados:**
- Nuevos: `entities/PlayerEntity.java`, `entities/MatchHistoryEntity.java`
- Nuevos: `repositories/PlayerRepository.java`, `repositories/MatchHistoryRepository.java`
- Nuevos: `services/RankingService.java`
- Nuevos: `controllers/RankingController.java`
- Nuevos: `features/ranking/` (feature completa en FE)

**Estimación:** 5-7 días

### 4. Chat en Partida (incluido en bonus)

Chat en tiempo real entre jugadores durante la partida.

**Implementación:**
- Extender el WebSocket existente con mensajes de chat
- Nuevo tipo de mensaje: `ChatMessage`
- Componente de chat en el Game Board

**Archivos afectados:**
- `websocket/messages/ChatMessage.java` (nuevo)
- `GameWebSocketHandler.java` (modificar)
- Nuevo componente `chat/` en `features/game/components/`

**Estimación:** 2-3 días

## Prioridad Recomendada

Si se deciden implementar las funcionalidades bonus, el orden recomendado es:

1. **Animaciones** (5 pts) - Puramente FE, bajo riesgo, alto impacto visual
2. **Deck temático** (5 pts) - BE simple, rápido de implementar
3. **Ranking** (5 pts) - Requiere nuevas entidades y feature completa
4. **Chat** (incluido) - Extensión del WebSocket existente

## Estimación Total Post-Proyecto

| Funcionalidad | Tiempo | Dependencias |
|---|---|---|
| Animaciones | 3-5 días | Game Board completo |
| Deck temático | 2-3 días | Seed data funcional |
| Ranking | 5-7 días | Matches funcionales |
| Chat | 2-3 días | WebSocket funcional |
| **Total** | **12-18 días** | Proyecto obligatorio completo |

## Consideraciones

- **No comenzar** hasta tener los 100 puntos obligatorios confirmados
- **Mantener ramas separadas** para cada funcionalidad bonus
- **No mezclar** código bonus con código obligatorio hasta que esté completo
- **Testing:** Las funcionalidades bonus también deben tener tests, pero no cuentan para el JaCoCo obligatorio
