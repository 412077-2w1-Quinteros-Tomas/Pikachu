<p align="center"> <img src="./BE/docs/assets/images/pokemon-tcg.png" alt="Pokemon TCG"/> </p>

# Pokémon Trading Card Game — Pikachu

Versión digital del Pokémon TCG desarrollada como proyecto final de **Programación III** (UTN FRC).

Permite crear mazos, unirse a partidas multijugador en tiempo real mediante WebSockets, y jugar partidas completas con el motor de juego implementado en el backend.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 4.0.0 · Java 21 · H2 (dev) / PostgreSQL (prod) |
| Frontend | Angular 20 · TypeScript · Signals API |
| Comunicación | REST API + WebSockets |
| Testing BE | JUnit 5 · Mockito · JaCoCo |
| Testing FE | Karma · Jasmine |

---

## Requisitos

- **Java 21** (o superior)
- **Maven 3.9+**
- **Node.js 20+** y **npm**
- Conexión a internet (para la primera sincronización de cartas desde pokemontcg.io)

---

## Cómo correr el proyecto (desarrollo)

### 1. Backend

```bash
cd BE
mvn spring-boot:run
```

El servidor inicia en `http://localhost:8080`.  
La base de datos H2 es **en memoria** — se borra al reiniciar.

### 2. Frontend

```bash
cd FE
npx ng serve --port 4201
```

La app queda disponible en `http://localhost:4201`.

### 3. Sincronizar cartas (obligatorio tras cada reinicio del BE)

En el navegador, ir a **Pokédex** y hacer clic en **"Sincronizar cartas"**.  
Esto descarga las ~180 cartas del set XY1 desde la API de pokemontcg.io.

---

## Flujo de juego

1. **Pokédex** — explorar cartas disponibles
2. **Mazo Builder** — crear 2 mazos válidos (exactamente 60 cartas, máx. 4 copias por carta, al menos 1 Básico)
3. **Lobby** → "Nueva Partida" con tu nombre y mazo
4. Abrir otra pestaña → **Lobby** → unirse a la partida con el segundo jugador
5. Jugar: adjuntar energía, atacar, retirar, terminar turno

---

## Variables de entorno (opcionales)

| Variable | Descripción | Default |
|----------|-------------|---------|
| `POKEMONTCG_API_KEY` | API key de pokemontcg.io (aumenta rate limit) | *(sin clave)* |

---

## Perfil de producción (PostgreSQL)

```bash
cd BE
mvn spring-boot:run -Dspring.profiles.active=prod \
  -DDB_URL=jdbc:postgresql://localhost:5432/pokemon_tcg \
  -DDB_USER=postgres \
  -DDB_PASSWORD=secret
```

Ejecutar el script de migración antes del primer arranque:

```bash
psql -U postgres -d pokemon_tcg -f src/main/resources/db/migration/V1__init.sql
```

---

## URLs útiles

| Recurso | URL |
|---------|-----|
| Frontend | http://localhost:4201 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

---

## Correr los tests

```bash
# Backend (incluye JaCoCo coverage report)
cd BE && mvn verify

# Frontend
cd FE && npx ng test --watch=false
```

El reporte de cobertura JaCoCo se genera en `BE/target/site/jacoco/index.html`.

---

## Arquitectura

```
┌─────────────────────────────────────┐
│  Frontend (Angular 20)              │
│  Signals · OnPush · Lazy Loading    │
│  WebSocket client (native WS)       │
└────────────┬────────────────────────┘
             │  REST + WebSocket
┌────────────▼────────────────────────┐
│  Backend (Spring Boot 4.0)          │
│  REST API (/api/*)                  │
│  WebSocket (/ws/game)               │
│                                     │
│  Game Engine                        │
│  ├── State Machine (4 estados)      │
│  ├── Combat Pipeline (8 pasos)      │
│  ├── Status Effects (5 condiciones) │
│  └── Reglas TCG                     │
│                                     │
│  H2 (dev) / PostgreSQL (prod)       │
└─────────────────────────────────────┘
```

---

<p align="center">
  <img src="./BE/docs/assets/images/UTN-FRC_logo.png" alt="UTN - FRC" width="200"/>
  &nbsp;&nbsp;&nbsp;
  <img src="./BE/docs/assets/images/Tup_completo_negro_transparente.png" alt="TUP" width="150"/>
</p>
