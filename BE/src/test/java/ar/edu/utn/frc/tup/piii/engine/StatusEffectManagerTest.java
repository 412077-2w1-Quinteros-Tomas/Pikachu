package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.effects.AsleepEffect;
import ar.edu.utn.frc.tup.piii.engine.effects.BurnedEffect;
import ar.edu.utn.frc.tup.piii.engine.effects.ConfusedEffect;
import ar.edu.utn.frc.tup.piii.engine.effects.ParalyzedEffect;
import ar.edu.utn.frc.tup.piii.engine.effects.PoisonedEffect;
import ar.edu.utn.frc.tup.piii.engine.effects.StatusEffectManager;
import ar.edu.utn.frc.tup.piii.engine.effects.StatusEffectStrategy;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusEffectManagerTest {

    private StatusEffectManager manager;

    @BeforeEach
    void setUp() {
        List<StatusEffectStrategy> strategies = List.of(
                new AsleepEffect(), new BurnedEffect(), new ConfusedEffect(),
                new ParalyzedEffect(), new PoisonedEffect()
        );
        manager = new StatusEffectManager(strategies);
    }

    @Test
    void canAttack_noCondition_returnsTrue() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, null);
        assertThat(manager.canAttack(pokemon)).isTrue();
    }

    @Test
    void canAttack_paralyzed_returnsFalse() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, SpecialCondition.PARALYZED);
        assertThat(manager.canAttack(pokemon)).isFalse();
    }

    @Test
    void canAttack_asleep_returnsFalse() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, SpecialCondition.ASLEEP);
        assertThat(manager.canAttack(pokemon)).isFalse();
    }

    @Test
    void canAttack_poisoned_returnsTrue() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, SpecialCondition.POISONED);
        assertThat(manager.canAttack(pokemon)).isTrue();
    }

    @Test
    void canRetreat_paralyzed_returnsFalse() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, SpecialCondition.PARALYZED);
        assertThat(manager.canRetreat(pokemon)).isFalse();
    }

    @Test
    void canRetreat_noCondition_returnsTrue() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, null);
        assertThat(manager.canRetreat(pokemon)).isTrue();
    }

    @Test
    void poisonedEffect_appliesDamage() {
        PokemonInPlay pokemon = buildPokemon("Bulbasaur", 60, SpecialCondition.POISONED);
        PlayerBoard board = new PlayerBoard();
        board.setPlayerId("p1");
        board.setActivePokemon(pokemon);
        board.setHand(new ArrayList<>());
        board.setDeck(new ArrayList<>());
        board.setBench(new ArrayList<>());
        board.setPrizeCards(new ArrayList<>());

        GameEventPublisher pub = new GameEventPublisher();
        manager.applyBetweenTurnEffects(board, pub);

        assertThat(board.getActivePokemon().getCurrentHp()).isEqualTo(50);
    }

    @Test
    void burnedEffect_appliesDamage() {
        PokemonInPlay pokemon = buildPokemon("Charmander", 60, SpecialCondition.BURNED);
        PlayerBoard board = new PlayerBoard();
        board.setPlayerId("p1");
        board.setActivePokemon(pokemon);
        board.setHand(new ArrayList<>());
        board.setDeck(new ArrayList<>());
        board.setBench(new ArrayList<>());
        board.setPrizeCards(new ArrayList<>());

        GameEventPublisher pub = new GameEventPublisher();
        manager.applyBetweenTurnEffects(board, pub);

        assertThat(board.getActivePokemon().getCurrentHp()).isLessThanOrEqualTo(40);
    }

    @Test
    void paralyzedEffect_removedAfterBetweenTurns() {
        PokemonInPlay pokemon = buildPokemon("Pikachu", 60, SpecialCondition.PARALYZED);
        PlayerBoard board = new PlayerBoard();
        board.setPlayerId("p1");
        board.setActivePokemon(pokemon);
        board.setHand(new ArrayList<>());
        board.setDeck(new ArrayList<>());
        board.setBench(new ArrayList<>());
        board.setPrizeCards(new ArrayList<>());

        GameEventPublisher pub = new GameEventPublisher();
        manager.applyBetweenTurnEffects(board, pub);

        assertThat(board.getActivePokemon().getSpecialCondition()).isNull();
    }

    @Test
    void applyBetweenTurnEffects_noActiveP_doesNothing() {
        PlayerBoard board = new PlayerBoard();
        board.setPlayerId("p1");
        board.setActivePokemon(null);
        board.setHand(new ArrayList<>());
        board.setDeck(new ArrayList<>());
        board.setBench(new ArrayList<>());
        board.setPrizeCards(new ArrayList<>());

        GameEventPublisher pub = new GameEventPublisher();
        manager.applyBetweenTurnEffects(board, pub);
    }

    private PokemonInPlay buildPokemon(String name, int hp, SpecialCondition condition) {
        PokemonCard card = new PokemonCard(name.toLowerCase(), name, hp,
                PokemonStage.BASIC, List.of(EnergyType.GRASS),
                new ArrayList<>(), null, null, 1, null);
        PokemonInPlay pip = PokemonInPlay.of(card);
        pip.setSpecialCondition(condition);
        return pip;
    }
}
