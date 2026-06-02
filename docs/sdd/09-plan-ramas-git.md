# 09 - Plan de Ramas Git

## Estrategia de Branching (GitFlow)

```
main ─────────────────────────────────────────────────────────► (producción)
  ╲
   develop ───────────────────────────────────────────────────► (integración)
     │
     ├── feature/cartas-api          (P1 - BE)
     ├── feature/deck-builder-be     (P1 - BE)
     ├── feature/matches-ws          (P2 - BE)
     ├── feature/game-engine-state   (P3 - BE)
     ├── feature/game-engine-combat  (P4 - BE)
     ├── feature/game-engine-effects (P4 - BE)
     ├── feature/pokedex             (P5 - FE)
     ├── feature/deck-builder-fe     (P5 - FE)
     ├── feature/lobby               (P6 - FE)
     └── feature/game-board          (P6 - FE)
```

## Asignación de Personas

| Persona | Rol | Ramas | Semanas |
|---|---|---|---|
| **P1** | BE Cards/Decks | `feature/cartas-api`, `feature/deck-builder-be` | 1-2 |
| **P2** | BE Matches/WS | `feature/matches-ws` | 3 |
| **P3** | BE Engine (State) | `feature/game-engine-state` | 4 |
| **P4** | BE Engine (Combat) | `feature/game-engine-combat`, `feature/game-engine-effects` | 4-5 |
| **P5** | FE Pokedex/Decks | `feature/pokedex`, `feature/deck-builder-fe` | 1-2 |
| **P6** | FE Lobby/Game | `feature/lobby`, `feature/game-board` | 3-5 |

## Reglas de Branching

1. **`main`** solo recibe merges de `develop` (releases)
2. **`develop`** es la rama de integración
3. **`feature/*`** se crea desde `develop` actualizado
4. **Cada feature tiene su propia rama** - no se comparten ramas
5. **PRs a `develop` requieren al menos 1 review** de otro integrante
6. **Antes de hacer PR, hacer `git rebase develop`** en la rama feature

## Flujo de Integración Semanal

```
Lunes:     Cada persona crea/actualiza su rama desde develop
Miércoles: Rebase de develop → feature (traer cambios de otros)
Viernes:   PR a develop (review cruzado entre 2 personas)
Sábado:    Merge a develop (solo si pasa CI)
```

## Dependencias entre Ramas

```
Semana 1:
  feature/cartas-api (P1) ──────────────────┐
  feature/pokedex (P5) ─────────────────────┘ (independientes, paralelo)

Semana 2:
  feature/deck-builder-be (P1) ── depende de ── feature/cartas-api
  feature/deck-builder-fe (P5) ── depende de ── feature/pokedex + cartas-api

Semana 3:
  feature/matches-ws (P2) ──────────────────┐
  feature/lobby (P6) ───────────────────────┘ (independientes de semana 2)

Semana 4:
  feature/game-engine-state (P3) ───────────┐
  feature/game-engine-combat (P4) ── depende de ── game-engine-state

Semana 5:
  feature/game-engine-effects (P4) ── depende de ── game-engine-combat
  feature/game-board (P6) ── depende de ── matches-ws + game-engine (completo)
```

## Archivos Compartidos (Puntos de Conflicto)

| Archivo | Quién lo toca | Cuándo | Cómo se maneja |
|---|---|---|---|
| `application.properties` | P1 | Semana 1 | Se define una vez, nadie más lo modifica |
| `pom.xml` | P1 | Semana 1 | Se define una vez, nadie más lo modifica |
| `.gitignore` | P1 | Semana 1 | Se define una vez |
| `app.config.ts` | P5 (semana 1), P6 (semana 3) | Semanas 1, 3 | P5 agrega HttpClient, P6 agrega WebSocket providers |
| `app.routes.ts` | P5 (semana 2), P6 (semana 3) | Semanas 2, 3 | Cada uno agrega sus rutas en su rama |
| `enums/*` (BE) | P1 | Semana 1 | Se definen todos de una vez |
| `exceptions/*` (BE) | P1 | Semana 1 | Se implementan todos de una vez |
| `shared/models/` (FE) | P5 (card, deck), P6 (match, game) | Semanas 1, 3 | Archivos separados, sin conflicto |

## Convención de Commits

```
<type>(<scope>): <description>

Types:
  feat     - Nueva funcionalidad
  fix      - Corrección de bug
  docs     - Documentación
  style    - Formato, sin cambio de código
  refactor - Refactorización
  test     - Tests
  chore    - Tareas de mantenimiento

Ejemplos:
  feat(card): implementar CardEntity y CardRepository
  feat(deck): agregar validación de mazo
  fix(engine): corregir cálculo de daño con debilidad
  docs(sdd): actualizar diagrama de secuencia
  test(service): agregar tests de DeckService
```

## Pull Request Template

```markdown
## Descripción
<!-- Qué cambia y por qué -->

## Cambios
<!-- Lista de cambios principales -->

## Testing
<!-- Qué se probó y cómo -->

## Dependencias
<!-- ¿Depende de otro PR? -->

## Checklist
- [ ] Código compila sin errores
- [ ] Tests pasan
- [ ] No hay conflictos con develop
- [ ] Documentación actualizada (si aplica)
```

## CI Básico (GitHub Actions)

Cada PR a `develop` debe pasar:

```yaml
# .github/workflows/ci.yml
name: CI

on:
  pull_request:
    branches: [develop]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
      - name: Build & Test
        run: |
          cd BE
          mvn clean test
          mvn checkstyle:check
          mvn pmd:check

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      - name: Install & Build
        run: |
          cd FE
          npm ci
          npm run build
          npm test
```
