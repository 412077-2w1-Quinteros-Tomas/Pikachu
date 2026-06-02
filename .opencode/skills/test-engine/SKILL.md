---
name: test-engine
description: Writes tests for the Pokemon TCG game engine and backend services achieving JaCoCo coverage targets. Use when writing JUnit 5 tests, Mockito tests, integration tests, or E2E tests for the Pokemon TCG project. Triggers on test, spec, coverage, JaCoCo, unit test, integration test, E2E, Mockito.
---

# Test Engine Skill - Pokemon TCG

You are writing tests for a Pokemon Trading Card Game backend. The project uses **JUnit 5**, **Mockito**, and **Spring Boot Test**. Coverage targets are strict: **≥80% global, ≥90% for critical components**.

## Testing Stack

- **JUnit 5** (`org.junit.jupiter`) - Test framework
- **Mockito** (`org.mockito`) - Mocking framework
- **Spring Boot Test** (`@SpringBootTest`) - Integration tests
- **MockMvc** - Controller tests
- **JaCoCo** - Code coverage (configured via Maven)
- **H2** - In-memory database for integration tests

## Coverage Requirements

| Component | Target Coverage |
|---|---|
| Global | **≥ 80%** instructions |
| `GameEngine` | **≥ 90%** |
| `DamageCalculator` | **≥ 90%** |
| `RuleValidator` | **≥ 90%** |
| `TurnManager` | **≥ 90%** |
| `VictoryConditionChecker` | **≥ 90%** |
| `StatusEffectManager` | **≥ 90%** |
| `DeckValidationService` | **≥ 90%** |
| `MatchService` | **≥ 90%** |

## Test Directory Structure

```
BE/src/test/java/ar/edu/utn/frc/tup/piii/
├── controllers/
│   ├── PingControllerTest.java          (exists)
│   ├── CardControllerTest.java
│   ├── DeckControllerTest.java
│   └── MatchControllerTest.java
├── services/
│   ├── CardServiceTest.java
│   ├── DeckServiceTest.java
│   ├── MatchServiceTest.java
│   └── GameStatePersistenceServiceTest.java
├── engine/
│   ├── state/
│   │   ├── WaitingStateTest.java
│   │   ├── SetupStateTest.java
│   │   ├── ActiveStateTest.java
│   │   └── FinishedStateTest.java
│   ├── combat/
│   │   ├── AttackResolverTest.java
│   │   ├── DamageCalculatorTest.java
│   │   └── steps/
│   │       ├── EnergyValidationStepTest.java
│   │       ├── ConfusionCheckStepTest.java
│   │       ├── TargetSelectionStepTest.java
│   │       ├── PreAttackEffectStepTest.java
│   │       ├── AttackModifierStepTest.java
│   │       ├── DamageCalculationStepTest.java
│   │       └── PostDamageEffectStepTest.java
│   ├── effects/
│   │   ├── AsleepEffectTest.java
│   │   ├── BurnedEffectTest.java
│   │   ├── ConfusedEffectTest.java
│   │   ├── ParalyzedEffectTest.java
│   │   ├── PoisonedEffectTest.java
│   │   └── StatusEffectManagerTest.java
│   ├── rules/
│   │   ├── RuleValidatorTest.java
│   │   ├── TurnManagerTest.java
│   │   └── VictoryConditionCheckerTest.java
│   └── GameEngineTest.java
├── mappers/
│   ├── CardMapperTest.java
│   ├── DeckMapperTest.java
│   └── MatchMapperTest.java
└── repositories/
    ├── CardRepositoryTest.java
    └── DeckRepositoryTest.java
```

## Test Conventions

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {

    @Mock
    private SomeRepository someRepository;

    @InjectMocks
    private SomeService someService;

    // Test methods...
}
```

### Controller Test Template

```java
@SpringBootTest
class CardControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllCards_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
```

### Repository Test Template

```java
@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Test methods...
}
```

## Critical Test Scenarios by Component

### GameEngine (≥ 90%)

The facade that delegates to all subsystems. Test that:
- Actions are properly delegated to state machine
- State transitions occur correctly
- Actions in wrong phase are rejected
- Events are published after actions
- State is persisted after actions

### DamageCalculator (≥ 90%)

Test cases for damage calculation:

| Scenario | Base | Weakness | Resistance | Expected |
|---|---|---|---|---|
| Normal hit | 30 | - | - | 30 |
| Super effective (weakness) | 30 | x2 | - | 60 |
| Resisted | 30 | - | -20 | 10 |
| Weakness + resistance | 30 | x2 | -20 | 40 |
| Minimum damage | 5 | - | -20 | 0 (min 0) |
| Damage counters | 33 | - | - | 4 counters (ceil(33/10)) |

### RuleValidator (≥ 90%)

Test cases:
- Deck validation: exactly 60 cards, max 4 per name, unlimited basic energy, max 1 ACE SPEC, min 1 Basic Pokemon
- Evolution validation: must evolve from correct base, cannot evolve same turn placed
- Energy attachment: max 1 per turn
- Retreat validation: must have enough energy for retreat cost

### TurnManager (≥ 90%)

Test cases:
- First turn: starting player skips draw phase
- Draw phase: player draws 1 card, deck empty = loss
- Main phase: track all flags (hasDrawn, hasAttachedEnergy, etc.)
- Attack phase: ends turn
- Between turns: process status effects in order

### StatusEffectManager (≥ 90%)

Test cases for each effect:

**BurnedEffect**:
- Between turns: add 2 damage counters
- Flip heads: remove condition
- Flip tails: keep condition
- Can coexist with Poisoned

**PoisonedEffect**:
- Between turns: add 1 damage counter
- Cannot coexist with another Poisoned
- Can coexist with Burned

**AsleepEffect**:
- Between turns: flip to wake
- Heads: wakes up, can act normally
- Tails: stays asleep, skips turn
- Mutually exclusive with Confused and Paralyzed

**ParalyzedEffect**:
- Skip next turn
- Auto-removes after turn is skipped
- Mutually exclusive with Asleep and Confused

**ConfusedEffect**:
- On attack: flip coin
- Heads: attack proceeds normally
- Tails: attack fails, 30 self-damage
- Mutually exclusive with Asleep and Paralyzed

**Incompatibility rules**:
- Applying Asleep removes Confused and Paralyzed
- Applying Confused removes Asleep and Paralyzed
- Applying Paralyzed removes Asleep and Confused
- Applying Burned/Poisoned does NOT remove Asleep/Confused/Paralyzed
- All conditions are cleared by evolution or retreat

### VictoryConditionChecker (≥ 90%)

Test cases:
- Win by taking last prize card
- Win by knockout (no bench Pokemon)
- Win by opponent deck empty
- Both players meet condition simultaneously → Sudden Death
- Game continues if no victory condition met

### Setup Phase (≥ 90%)

Test cases:
- Shuffle and draw 7 cards for both players
- Player with no Basic Pokemon: mulligan
- Mulligan: reveal hand, reshuffle, redraw
- Opponent draws 1 extra card per mulligan
- Place 1 Basic Pokemon as active (mandatory)
- Place up to 5 Basic Pokemon on bench (optional)
- Set 6 prize cards face down
- Coin flip to determine starting player

## Integration Test Scenarios

### Full Match Flow Test

Test a complete game from start to finish:

1. Create match
2. Both players join
3. Setup phase: draw, place basics, prize cards
4. Turn 1 (starting player): skip draw → main phase → end turn
5. Turn 2 (second player): draw → attach energy → attack
6. Continue until victory condition

### Deck Validation Integration Test

1. Create deck with 60 valid cards
2. Validate: should pass
3. Create deck with 59 cards: should fail
4. Create deck with 5 copies of same card: should fail
5. Create deck with 2 ACE SPEC cards: should fail
6. Create deck with no Basic Pokemon: should fail

## Running Tests and Coverage

```bash
# Run all tests
cd BE && mvn test

# Run specific test class
mvn test -Dtest=DamageCalculatorTest

# Run with coverage report
mvn test jacoco:report

# Coverage report location
# BE/target/site/jacoco/index.html

# Verify coverage thresholds
mvn verify
```

## JaCoCo Configuration

Add to `pom.xml` if not present:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
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
            <rule>
                <element>CLASS</element>
                <includes>
                    <include>ar.edu.utn.frc.tup.piii.engine.GameEngine</include>
                    <include>ar.edu.utn.frc.tup.piii.engine.combat.DamageCalculator</include>
                    <include>ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator</include>
                    <include>ar.edu.utn.frc.tup.piii.engine.rules.TurnManager</include>
                    <include>ar.edu.utn.frc.tup.piii.engine.rules.VictoryConditionChecker</include>
                    <include>ar.edu.utn.frc.tup.piii.engine.effects.StatusEffectManager</include>
                </includes>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.90</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

## E2E Test Requirements

At least **1 E2E test** is required. This should test the full flow:
1. Start the application
2. Create a match via REST
3. Connect via WebSocket
4. Perform game actions
5. Verify state changes

Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` with `TestRestTemplate` and a WebSocket test client.

## Checklist

When writing tests:
- [ ] Test class in correct package under `BE/src/test/java/ar/edu/utn/frc/tup/piii/`
- [ ] Uses JUnit 5 (`@Test`, `@BeforeEach`, `@BeforeEach`)
- [ ] Uses Mockito (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- [ ] Test method names are descriptive: `methodName_scenario_expectedResult`
- [ ] Each test has 3 phases: Arrange, Act, Assert
- [ ] Critical components have ≥90% coverage
- [ ] Edge cases tested (empty deck, KO, mulligan, sudden death)
- [ ] Status effect incompatibility rules tested
- [ ] Damage calculation edge cases tested (weakness, resistance, minimum)
- [ ] At least 1 E2E test exists
- [ ] JaCoCo configured with ≥80% global, ≥90% critical thresholds