# 13 - Checklist de Entrega

## Requisitos Obligatorios (100 puntos)

### Reglas del Juego (RF-01 a RF-07) - 40 puntos

| Requisito | Descripción | Estado |
|---|---|---|
| RF-01 | Reglas del juego (setup, turnos, ataque, KO, condiciones, victoria) | [ ] |
| RF-02 | Tipos de cartas (Pokémon, Energy, Trainer con subtipos) | [ ] |
| RF-03 | Gestión de partidas (estados, source of truth, persistencia) | [ ] |
| RF-04 | Deck Builder (set xy1, validación, CRUD) | [ ] |
| RF-05 | Persistencia de estado (board, manos, log de acciones) | [ ] |
| RF-06 | Comunicación real-time (WebSocket, sync, reconexión) | [ ] |
| RF-07 | Interfaz de usuario (lobby, board, drag&drop, panel de acciones) | [ ] |

### Arquitectura & Código - 25 puntos

| Requisito | Descripción | Estado |
|---|---|---|
| SOLID | Principios SOLID aplicados | [ ] |
| Engine aislado | GameEngine separado con RuleValidator, DamageCalculator, etc. | [ ] |
| Patrones de diseño | State, Strategy, Chain of Responsibility, Observer, Repository, Facade | [ ] |
| Código limpio | Nombres claros, responsabilidad única, sin duplicación | [ ] |
| Angular style guide | Standalone components, signals, OnPush, input()/output() | [ ] |

### Base de Datos - 10 puntos

| Requisito | Descripción | Estado |
|---|---|---|
| Schema relacional | Tablas: cards, decks, deck_cards, matches, game_states | [ ] |
| Migraciones versionadas | Scripts SQL en `db/migration/` | [ ] |
| Seed data | Cartas del set xy1 cargadas | [ ] |
| Índices | Índices en columnas de búsqueda y FK | [ ] |

### Frontend - 15 puntos

| Requisito | Descripción | Estado |
|---|---|---|
| Lobby | Crear y unirse a partidas | [ ] |
| Board interactivo | Drag & drop para colocar Pokémon, attachar energía, etc. | [ ] |
| Visualización en tiempo real | HP, daño, energía, herramientas, condiciones, premios | [ ] |
| Panel de acciones | Botones contextuales habilitados/deshabilitados | [ ] |
| Log de acciones | Historial de acciones de la partida | [ ] |
| Notificaciones visuales | Feedback al usuario | [ ] |

### Testing - 10 puntos

| Requisito | Descripción | Estado |
|---|---|---|
| JaCoCo global | ≥ 80% de cobertura de instrucciones | [ ] |
| JaCoCo crítico | ≥ 90% en componentes críticos del engine | [ ] |
| Tests de integración | Flujo completo de partida | [ ] |
| Test E2E | Al menos 1 test end-to-end | [ ] |

## Entregables

| Entregable | Descripción | Estado |
|---|---|---|
| Código fuente | Repo con README (instalación, ejecución, stack, arquitectura) | [ ] |
| Script SQL | Schema + migraciones versionadas + seed data | [ ] |
| Documentación técnica | API spec (Swagger), decisiones de diseño, manual de deploy | [ ] |
| Reporte JaCoCo | Generado con `mvn jacoco:report` | [ ] |

## Checklist de Código

### Backend

- [ ] Todos los tests pasan (`mvn test`)
- [ ] JaCoCo ≥ 80% (`mvn jacoco:check`)
- [ ] Checkstyle sin violaciones (`mvn checkstyle:check`)
- [ ] PMD sin violaciones (`mvn pmd:check`)
- [ ] Swagger UI accesible (`/swagger-ui.html`)
- [ ] API key no commiteada
- [ ] `application.properties` sin secrets

### Frontend

- [ ] Build sin errores (`npm run build`)
- [ ] Tests unitarios pasan (`npm test`)
- [ ] Test E2E pasa
- [ ] No hay `any` en TypeScript (usar `unknown`)
- [ ] No hay `ngClass`/`ngStyle`
- [ ] No hay `*ngIf`/`*ngFor` (usar `@if`/`@for`)
- [ ] No hay `@HostBinding`/`@HostListener`
- [ ] No hay `standalone: true` explícito

### Git

- [ ] No hay secrets en el historial de commits
- [ ] `develop` está actualizado
- [ ] No hay ramas feature sin mergear
- [ ] Commits siguen convención de nombres
