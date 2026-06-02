# 10 - Testing

## Estrategia de Testing

### Backend

| Nivel | Objetivo | Herramientas |
|---|---|---|
| Unit | Services, Engine, Validators, Utils | JUnit 5, Mockito |
| Integration | Controllers (request/response completo) | MockMvc, @SpringBootTest |
| Integration | Flujo completo del juego | @SpringBootTest |
| Cobertura | ≥ 80% global, ≥ 90% crítico | JaCoCo |

### Frontend

| Nivel | Objetivo | Herramientas |
|---|---|---|
| Unit | Components, Services, Pipes | Jasmine, Karma |
| E2E | Mínimo 1 flujo completo de juego | Cypress o Playwright |

## Componentes Críticos (Cobertura ≥ 90%)

Estos componentes son el núcleo del sistema y requieren cobertura de testing máxima:

| Componente | Archivo | Razón |
|---|---|---|
| GameEngine | `engine/GameEngine.java` | Orquestador principal del juego |
| DamageCalculator | `engine/combat/DamageCalculator.java` | Cálculo de daño (debilidad, resistencia, modificadores) |
| RuleValidator | `engine/rules/RuleValidator.java` | Validación de todas las acciones |
| TurnManager | `engine/rules/TurnManager.java` | Gestión de turnos y fases |
| VictoryConditionChecker | `engine/rules/VictoryConditionChecker.java` | Determina ganador |
| StatusEffectManager | `engine/effects/StatusEffectManager.java` | Gestión de condiciones especiales |
| DeckValidationService | `services/DeckValidationService.java` | Validación de composición de mazo |
| MatchService | `services/MatchService.java` | Ciclo de vida de partidas |

## Tests Backend Recomendados

### Unit Tests

```
CardServiceTest.java          - CRUD de cartas
DeckServiceTest.java          - CRUD de mazos
DeckValidationServiceTest.java - Reglas de validación de mazo
MatchServiceTest.java         - CRUD de partidas
GameEngineTest.java           - State machine, transiciones
DamageCalculatorTest.java     - Cálculo de daño (casos borde)
RuleValidatorTest.java        - Validación de acciones
TurnManagerTest.java          - Gestión de turnos
VictoryConditionCheckerTest.java - Condiciones de victoria
StatusEffectManagerTest.java  - Efectos de estado
BurnedEffectTest.java         - Efecto quemado
PoisonedEffectTest.java       - Efecto envenenado
AsleepEffectTest.java         - Efecto dormido
ParalyzedEffectTest.java      - Efecto paralizado
ConfusedEffectTest.java       - Efecto confundido
```

### Integration Tests

```
CardControllerTest.java       - Endpoints de cartas
DeckControllerTest.java       - Endpoints de mazos
MatchControllerTest.java      - Endpoints de partidas
GameEngineIntegrationTest.java - Flujo completo de partida
WebSocketIntegrationTest.java  - Comunicación WebSocket
```

## Tests Frontend Recomendados

### Unit Tests

```
card.service.spec.ts          - Servicio de cartas
deck.service.spec.ts          - Servicio de mazos
lobby.service.spec.ts         - Servicio de lobby
game-state.service.spec.ts    - Servicio de estado del juego
card.component.spec.ts        - Componente de carta
game-board.component.spec.ts  - Componente del tablero
action-panel.component.spec.ts - Panel de acciones
```

### E2E Tests (mínimo 1)

Escenario recomendado: Flujo completo de partida

```
1. Crear partida desde el lobby
2. Unirse a la partida
3. Completar setup (colocar Pokémon)
4. Realizar acciones (attachar energía, atacar)
5. Verificar que el estado se actualiza en tiempo real
6. Verificar condición de victoria
```

## Configuración JaCoCo

Agregar al `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Ejecución de Tests

```bash
# Backend
mvn test                    # Ejecutar todos los tests
mvn test -Dtest=CardServiceTest  # Ejecutar test específico
mvn jacoco:report           # Generar reporte de cobertura

# Frontend
npm test                    # Ejecutar tests unitarios
ng e2e                      # Ejecutar tests E2E
```
