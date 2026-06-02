# Semana 1 — Resumen de Implementación

## Backend

---

### Tarea 1 — Configurar `.gitignore` para secrets

**Archivos:** `.gitignore` (root), `BE/.gitignore`

Se agregó una sección `### Secrets & Credentials ###` en el `.gitignore` raíz con patrones
genéricos que protegen cualquier archivo de credenciales en todo el repositorio:
`.env`, `.env.*`, `*.env`, `*.secret`, `*.key`, `*.pem`, `*.p12`, `*.jks`, `secrets.properties`.

En `BE/.gitignore` se agregaron los patrones específicos de Spring Boot:
`application-local.properties` (donde cada desarrollador pone su API key local),
`application-secrets.properties` y `.env`.

De esta forma ninguna clave real puede llegar al repositorio por accidente.

---

### Tarea 2 — Configurar API key por variable de entorno

**Archivos:** `BE/src/main/resources/application.properties`,
`BE/src/main/resources/application-local.properties.example`

Se agregaron dos propiedades en `application.properties`:

```
pokemontcg.api.base-url=https://api.pokemontcg.io/v2
pokemontcg.api.key=${POKEMONTCG_API_KEY:}
```

El placeholder `${POKEMONTCG_API_KEY:}` hace que Spring Boot lea la clave desde la
variable de entorno `POKEMONTCG_API_KEY`. Si no existe, queda vacía y la app arranca
igual (la API de pokemontcg.io funciona sin clave pero con límite de tasa).

Se creó `application-local.properties.example` como plantilla que cada desarrollador
copia a `application-local.properties` (ignorado por git) y completa con su clave real.

---

### Tarea 3 — Implementar BaseEntity

**Archivos:** `BE/src/main/java/.../entities/BaseEntity.java`, `Application.java`

Se implementó `BaseEntity` como clase abstracta con `@MappedSuperclass`. Todas las
entidades del proyecto la extienden y heredan automáticamente:

- `id`: UUID generado automáticamente por Hibernate con `GenerationType.UUID`.
- `createdAt`: fecha de creación, seteada una sola vez al persistir (`updatable = false`).
- `updatedAt`: fecha de última modificación, actualizada en cada save.

Los campos de auditoría (`createdAt`, `updatedAt`) son gestionados por Spring Data JPA
via `@CreatedDate`, `@LastModifiedDate` y `@EntityListeners(AuditingEntityListener.class)`.

Se agregó `@EnableJpaAuditing` en `Application.java` para activar ese mecanismo.

---

### Tarea 4 — Implementar todos los enums

**Archivos:** `BE/src/main/java/.../enums/*.java` (8 archivos)

Se implementaron los 8 enums que representan los tipos de datos del juego:

- `CardType`: POKEMON, ENERGY, TRAINER.
- `EnergyType`: los 11 tipos de energía (FIRE, WATER, GRASS, LIGHTNING, PSYCHIC,
  FIGHTING, DARKNESS, METAL, FAIRY, DRAGON, COLORLESS). Incluye un campo `apiName`
  con el string que devuelve pokemontcg.io y un método estático `fromApiName()` para
  convertir la respuesta de la API al enum interno.
- `PokemonStage`: BASIC, STAGE1, STAGE2, EX, MEGA.
- `TrainerSubtype`: ITEM, SUPPORTER, STADIUM, TOOL, PLASMA. Sin ACE_SPEC porque
  el set XY1 no contiene cartas de ese subtipo.
- `SpecialCondition`: ASLEEP, BURNED, CONFUSED, PARALYZED, POISONED.
- `GamePhase`: WAITING, SETUP, DRAW, MAIN, ATTACK, BETWEEN_TURNS, FINISHED.
- `TurnPhase`: DRAW, ACTIONS, ATTACK, BETWEEN_TURNS.
- `GameZone`: HAND, DECK, DISCARD, ACTIVE, BENCH, PRIZE, STADIUM, TOOL, ENERGY.

---

### Tarea 5 — Implementar excepciones custom

**Archivos:** `BE/src/main/java/.../exceptions/` (4 archivos)

Se implementaron las 4 excepciones de dominio del proyecto:

- `EntityNotFoundException`: lanzada cuando no se encuentra una entidad en la base de
  datos. Incluye el método estático `EntityNotFoundException.of("Card", id)` para
  generar mensajes descriptivos sin repetir el formato en cada service.
- `DeckValidationException`: lanzada cuando un mazo no cumple las reglas de validación.
  Lleva una `List<String> errors` con todos los errores acumulados, lo que permite
  devolver todos los problemas del mazo en una sola respuesta en lugar de uno por uno.
- `InvalidActionException`: lanzada cuando el jugador intenta una acción que no está
  permitida en el estado actual del juego (por ejemplo, atacar durante la fase MAIN).
  Corresponde a HTTP 409.
- `GameRuleViolationException`: lanzada cuando se viola una regla del engine (por
  ejemplo, intentar attachar dos energías en el mismo turno). También HTTP 409.

Todas extienden `RuntimeException` para no requerir declaración en las firmas de métodos.

---

### Tarea 6 — Implementar GlobalExceptionHandler

**Archivos:** `BE/src/main/java/.../exceptions/GlobalExceptionHandler.java`

Se implementó el manejador global de excepciones con `@RestControllerAdvice`. Captura
todas las excepciones y las convierte en respuestas HTTP con el formato `ErrorApi`
(timestamp, status, error, message):

- `EntityNotFoundException` → 404 Not Found.
- `DeckValidationException` → 400 Bad Request (une todos los errores de la lista con "; ").
- `InvalidActionException` → 409 Conflict.
- `GameRuleViolationException` → 409 Conflict.
- `MethodArgumentNotValidException` → 400 Bad Request (errores de `@Valid` formateados
  como "campo: mensaje").
- `Exception` (genérica) → 500 Internal Server Error.

Todos los casos pasan por un método privado `build()` que construye el `ErrorApi`
evitando repetición de código.

---

### Tarea 7 — Implementar CorsConfig

**Archivos:** `BE/src/main/java/.../configs/CorsConfig.java`

Se implementó `CorsConfig` como `@Configuration` que implementa `WebMvcConfigurer`.
Configura CORS para permitir que el frontend Angular (puerto 4200) llame al backend
(puerto 8080) durante el desarrollo:

- `allowedOrigins`: `http://localhost:4200`.
- `allowedMethods`: GET, POST, PUT, DELETE, OPTIONS (OPTIONS es necesario para el
  preflight que hacen los navegadores antes de cada request cross-origin).
- `allowedHeaders`: `*` (cualquier header).
- `allowCredentials`: true (necesario para WebSocket con STOMP).

---

### Tarea 8 — Implementar CardEntity

**Archivos:** `BE/src/main/java/.../entities/CardEntity.java`

Se implementó `CardEntity` como entidad JPA que extiende `BaseEntity`. Representa una
carta del TCG almacenada en la base de datos. Campos principales:

- `externalId`: ID de la API de pokemontcg.io (único, usado para evitar duplicados al
  sincronizar).
- `name`, `cardType`, `hp`, `stage`, `weakness`, `resistance`, `retreatCost`,
  `imageUrl`, `rarity`, `cardNumber`, `setId`: campos simples.
- `types`, `attacks`, `abilities`: almacenados como JSON en columnas TEXT. Esto es
  compatible con H2 (dev) y PostgreSQL (prod) sin necesidad de tablas adicionales.
  La serialización/deserialización la maneja el mapper con Jackson.
- `cardType`: campo adicional al SDD para distinguir POKEMON/ENERGY/TRAINER sin
  necesidad de parsear los campos JSON.
- `setId`: valor por defecto `"xy1"`, el set obligatorio del proyecto.

Se usó `@Getter`, `@Setter` y `@NoArgsConstructor` de Lombok. Sin `@Builder` en la
entidad para seguir el patrón idiomático de JPA donde el acceso es por setters.

---

### Tarea 9 — Implementar CardRepository

**Archivos:** `BE/src/main/java/.../repositories/CardRepository.java`

Se implementó `CardRepository` extendiendo `JpaRepository<CardEntity, UUID>`. Hereda
automáticamente todo el CRUD base y agrega 4 queries derivadas específicas del dominio:

- `findByExternalId(String)`: buscar por ID de la API externa para el proceso de sync.
- `existsByExternalId(String)`: chequeo de existencia antes de insertar, sin cargar el
  objeto completo.
- `findBySetId(String)`: listar todas las cartas de un set determinado.
- `findByNameContainingIgnoreCase(String)`: búsqueda de texto parcial insensible a
  mayúsculas para el buscador del Pokedex.

---

### Tarea 10 — Implementar CardDTO + CardFilterDTO

**Archivos:** `BE/src/main/java/.../dtos/card/CardDTO.java`,
`BE/src/main/java/.../dtos/card/CardFilterDTO.java`

`CardDTO` representa la carta tal como la ve el cliente en la API REST. Incluye todas las
mismas propiedades que `CardEntity` pero con los campos JSON ya deserializados como listas
tipadas (`List<EnergyType>`, `List<AttackDTO>`, `List<AbilityDTO>`).

`AttackDTO` y `AbilityDTO` se implementaron como clases internas estáticas de `CardDTO`
porque solo tienen sentido en ese contexto. `AttackDTO` usa `List<String>` para el costo
del ataque porque pokemontcg.io lo devuelve como lista de strings de tipo de energía.

`CardFilterDTO` agrupa los parámetros de filtrado del endpoint `GET /api/cards`. Tiene
valores por defecto de `page = 0` y `size = 20` para que el endpoint funcione sin
parámetros obligatorios.

---

### Tarea 11 — Implementar CardMapper

**Archivos:** `BE/src/main/java/.../mappers/CardMapper.java`

Se implementó `CardMapper` como `@Component` con lógica de conversión manual en lugar
de usar ModelMapper. Esto es necesario porque los campos `types`, `attacks` y `abilities`
están almacenados como JSON strings en la entidad y hay que deserializarlos con
`ObjectMapper` y `TypeReference` para convertirlos a listas tipadas.

Métodos implementados:
- `toDto(CardEntity)`: convierte entidad a DTO, deserializando los campos JSON.
- `toDtoList(List<CardEntity>)`: conversión en lote para listas de resultados.
- `toEntity(CardDTO)`: convierte DTO a entidad, serializando los campos a JSON.
- `updateEntity(CardEntity, CardDTO)`: actualiza los campos de una entidad existente sin
  crear una nueva instancia (evita el problema de no poder setear el ID en BaseEntity).

Los métodos privados `serialize()` y `deserialize()` encapsulan el manejo de Jackson y
devuelven `null` ante JSON inválido para no romper el servidor por datos corruptos.

---

### Tarea 12 — Implementar CardService (CRUD)

**Archivos:** `BE/src/main/java/.../services/CardService.java`

Se implementó `CardService` con los métodos que necesita el controller:

- `getById(UUID)`: busca por ID interno y lanza `EntityNotFoundException` si no existe.
- `getCards(CardFilterDTO)`: carga las cartas del set y aplica filtros en memoria. Dado
  que el set XY1 tiene 146 cartas, filtrar en Java es eficiente y evita la complejidad
  de JPA Specifications. Incluye paginación manual con `skip` y `limit`.
- `searchByName(String)`: delega en la query del repositorio.
- `save(CardDTO)`: inserción nueva.
- `saveOrUpdate(CardDTO)`: upsert por `externalId`. Busca si ya existe la carta; si
  existe, actualiza sus campos usando `cardMapper.updateEntity()`; si no, la inserta.
  Evita duplicados al re-sincronizar el set.
- `syncSet(String)`: orquesta la sincronización completa de un set, delegando en
  `ExternalCardApiService` para obtener las cartas y en `saveOrUpdate` para persistirlas.

---

### Tarea 13 — Implementar CardController

**Archivos:** `BE/src/main/java/.../controllers/CardController.java`

Se implementó `CardController` con `@RestController` en `/api/cards`. Implementa los
endpoints de lectura del SDD:

- `GET /api/cards`: acepta parámetros opcionales de filtro (`name`, `cardType`, `type`,
  `stage`, `setId`) y paginación (`page`, `size`). Construye un `CardFilterDTO` y delega
  en el service.
- `GET /api/cards/{id}`: obtiene una carta por UUID. El 404 lo maneja automáticamente
  el `GlobalExceptionHandler` si la carta no existe.
- `GET /api/cards/search`: búsqueda por nombre parcial.
- `POST /api/cards/sync`: recibe `{ "setId": "xy1" }` como body y devuelve
  `{ "synced": 146 }`. El request body se modela con un `record` Java 21 inline
  `SyncRequest` que evita crear un DTO separado para un campo de un solo uso.

---

### Tarea 14 — Integración API pokemontcg.io

**Archivos:** `BE/src/main/java/.../services/ExternalCardApiService.java`

Se implementó `ExternalCardApiService` que consume la API externa de pokemontcg.io v2
y mapea los resultados a `CardDTO`.

El cliente HTTP es `RestClient` (API moderna de Spring Boot 4 / Spring Framework 7,
reemplazo de `RestTemplate`). Se construye en `@PostConstruct` con la URL base y el
header `X-Api-Key` solo si la clave está configurada.

La paginación se implementa en un loop que consume páginas de 250 cartas hasta agotar
el `totalCount` de la respuesta. Para XY1 (146 cartas) es una sola request.

El mapeo de la respuesta de la API al modelo interno incluye:
- `supertype` ("Pokémon", "Trainer", "Energy") → `CardType`.
- `subtypes` (["Basic", "EX"]) → `PokemonStage`, tomando el primer subtipo reconocido.
- `types` (["Grass"]) → `List<EnergyType>` usando `EnergyType.fromApiName()`.
- `hp` (String) → Integer con manejo de `NumberFormatException`.
- `weaknesses[0].type` y `resistances[0].type` → strings de debilidad/resistencia.
- `retreatCost.size()` → Integer (cantidad de energías necesarias para retirarse).
- `images.small` → URL de la imagen.
- `attacks` y `abilities` → listas de `CardDTO.AttackDTO` y `CardDTO.AbilityDTO`.

Los modelos de respuesta de la API se definen como clases internas estáticas con
`@JsonIgnoreProperties(ignoreUnknown = true)` para ignorar campos no necesarios y
no romper ante cambios futuros de la API.

---

### Tarea 15 — Endpoint sync + seed data xy1

**Archivos:** `BE/src/main/java/.../controllers/CardController.java` (modificado),
`BE/src/main/java/.../services/CardService.java` (modificado)

Se agregó el método `syncSet(String setId)` en `CardService` que orquesta la
sincronización completa: llama a `ExternalCardApiService.fetchSetCards(setId)` para
obtener todas las cartas de la API y llama a `saveOrUpdate()` por cada una para
persistirlas. Retorna la cantidad de cartas sincronizadas.

Se agregó el endpoint `POST /api/cards/sync` en `CardController` que recibe
`{ "setId": "xy1" }` y retorna `{ "synced": 146 }`. El flujo completo es:

```
POST /api/cards/sync { "setId": "xy1" }
  → CardService.syncSet("xy1")
    → ExternalCardApiService.fetchSetCards("xy1")
      → GET https://api.pokemontcg.io/v2/cards?q=set.id:xy1
    → cardService.saveOrUpdate(dto) × 146
  → { "synced": 146 }
```

---

---

## Frontend

---

### Tarea 1 — Agregar `provideHttpClient` a app.config

**Archivos:** `FE/src/app/app.config.ts`

Se agregó `provideHttpClient(withInterceptors([errorInterceptor]))` al array de
`providers` de la aplicación. Esto registra el `HttpClient` de Angular en el árbol de
inyección de dependencias y encadena el interceptor de errores desde el inicio.

Se usa `withInterceptors()` con la función `errorInterceptor` (patrón funcional de
Angular 20) en lugar del antiguo `HTTP_INTERCEPTORS` con clase.

---

### Tarea 2 — Implementar error.interceptor

**Archivos:** `FE/src/app/core/interceptors/error.interceptor.ts`

Se implementó `errorInterceptor` como función (`HttpInterceptorFn`), siguiendo el
patrón funcional de Angular 20. Intercepta todos los errores HTTP con `catchError` y:

- Extrae el mensaje del campo `message` del `ErrorApi` que devuelve el backend.
- Clasifica el error por status code (0 = sin conexión, 404, 409, 500) y lo loguea.
- Re-lanza un objeto limpio `{ status, message }` para que los servicios y componentes
  lo consuman sin depender de `HttpErrorResponse`.

Los `console.error` son temporales y serán reemplazados por el componente
`NotificationToast` cuando se implemente en Semana 5.

---

### Tarea 3 — Implementar CardModel

**Archivos:** `FE/src/app/shared/models/card.model.ts`

Se definieron las interfaces y tipos que representan una carta en el frontend,
espejando exactamente el `CardDTO` del backend:

- `CardType`, `EnergyType`, `PokemonStage`: union types de TypeScript (más idiomáticos
  que enums para valores que vienen de JSON).
- `Attack` y `AbilityDTO`: interfaces para los objetos anidados de ataques y habilidades.
  `Attack.cost` es `string[]` porque la API devuelve el costo como lista de energías.
- `CardModel`: interfaz principal con todos los campos de la carta. Los campos que pueden
  ser nulos se tipan explícitamente con `| null` para coincidir con lo que devuelve el
  backend.
- `CardFilter`: interfaz de filtros de búsqueda que usa el `CardService`. Se incluye en
  el mismo archivo porque es el complemento directo del modelo.

---

### Tarea 4 — Implementar constantes

**Archivos:** `FE/src/app/core/constants/*.ts` (4 archivos)

Se implementaron las 4 constantes que la UI usa para labels, badges y filtros:

- `card-types.ts`: arrays `CARD_TYPES` y `POKEMON_STAGES` con el valor del enum y su
  label en español, para dropdowns y filtros del Pokedex y Deck Builder.
- `energy-types.ts`: array `ENERGY_TYPES` con valor, label, color hex y CSS class para
  cada tipo de energía. Se incluye `ENERGY_TYPE_MAP` (Map de ES6) para obtener la info
  de un tipo en O(1) sin iterar el array, útil en los badges de tipos.
- `game-phases.ts`: arrays `GAME_PHASES` y `TURN_PHASES` con valor, label y descripción
  para el panel de acciones del tablero. Incluye los types `GamePhase` y `TurnPhase`.
  `GAME_PHASE_MAP` para lookup rápido.
- `special-conditions.ts`: array `SPECIAL_CONDITIONS` con valor, label, descripción del
  efecto, color, CSS class y el campo `exclusiveWith` que lista las condiciones
  mutuamente excluyentes (ASLEEP, CONFUSED y PARALYZED no pueden coexistir). Útil para
  el componente `StatusIndicator`. `SPECIAL_CONDITION_MAP` para lookup rápido.

---

### Tarea 5 — Implementar CardService HTTP

**Archivos:** `FE/src/app/features/deck-builder/services/card.service.ts`

Se implementó `CardService` como servicio Angular con `inject(HttpClient)`. Cubre los
4 endpoints de cartas del backend:

- `getCards(filter)`: `GET /api/cards` con parámetros opcionales. Los filtros `null` o
  `undefined` se omiten del query string para no enviar params vacíos como `?name=`.
- `getById(id)`: `GET /api/cards/{id}`.
- `search(name)`: `GET /api/cards/search?name=`.
- `syncSet(setId)`: `POST /api/cards/sync` para poblar la base de datos desde la API
  externa.

Todos los métodos retornan `Observable` sin subscribirse, delegando esa responsabilidad
al componente o a `toSignal()`. Los errores los maneja automáticamente el
`errorInterceptor` registrado en `app.config.ts`.

---

### Tarea 6 — Implementar CardComponent

**Archivos:** `FE/src/app/shared/components/card/` (3 archivos)

Se implementó el componente compartido que muestra una carta del TCG. Es un componente
standalone con `ChangeDetectionStrategy.OnPush`.

**Entradas (inputs con signal API de Angular 20):**
- `card`: `CardModel` requerido.
- `selected`: booleano opcional (default `false`) para resaltar la carta seleccionada
  en el Deck Builder.
- `compact`: booleano opcional (default `false`) para mostrar solo la imagen sin info,
  útil para representar la mano del jugador en el tablero.

**Salida:**
- `clicked`: emite el `CardModel` al hacer clic, para que el padre decida la acción.

**Template:** usa la nueva sintaxis de control flow de Angular (`@if`, `@for`). Muestra
imagen, HP, tipos como badges coloreados, stage, lista de ataques con daño, y estadísticas
de debilidad/resistencia/retirada. Si no hay imagen disponible, muestra un placeholder
con la inicial del nombre.

**Estilos:** tema oscuro, proporción real de carta TCG (63:88), acento de color por tipo
via CSS custom property `--card-accent`, animación de hover con elevación, y estados de
foco accesibles con `focus-visible`.

---

### Tarea 7 — Implementar PokedexPage

**Archivos:** `FE/src/app/features/pokedex/pages/pokedex-page/` (3 archivos),
`FE/src/app/app.routes.ts`, `FE/proxy.conf.json`, `FE/angular.json`

Se implementó la página principal del Pokedex, el entregable funcional de la Semana 1.

**Estado reactivo con signals:**
- `nameFilter`, `cardTypeFilter`, `stageFilter`: signals que representan los filtros
  activos. El usuario los modifica via inputs del formulario.
- `filter`: `computed()` que combina los tres signals en un objeto `CardFilter`.
- `cards`: `toSignal()` sobre un observable que combina `toObservable(filter)` +
  `debounceTime(300)` + `switchMap(cardService.getCards)`. Esto hace que cada vez que
  el usuario cambia un filtro, se espera 300ms y se lanza una nueva request,
  cancelando la anterior si aún estaba pendiente.
- `loading` y `error`: signals para el estado de carga y errores.

**Template:** filtros (input de texto + dos selects), grilla de `CardComponent`,
estado de carga, estado vacío, y un panel de detalle inline que se abre al hacer clic
en una carta mostrando habilidades y ataques completos. El modal completo con
`card-detail-modal` se implementa en Semana 2.

**Rutas:** se actualizó `app.routes.ts` para usar `loadComponent` con lazy loading en
lugar de importar directamente el componente.

**Proxy:** se creó `proxy.conf.json` y se registró en `angular.json` para que el
servidor de desarrollo de Angular reenvíe las llamadas a `/api/*` y `/ws/*` al backend
en `http://localhost:8080`, evitando errores de CORS durante el desarrollo.
