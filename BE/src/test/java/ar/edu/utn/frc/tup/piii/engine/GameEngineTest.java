package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.rules.TurnManager;
import ar.edu.utn.frc.tup.piii.engine.rules.VictoryConditionChecker;
import ar.edu.utn.frc.tup.piii.engine.state.ActiveState;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import ar.edu.utn.frc.tup.piii.websocket.messages.GameActionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    private TurnManager turnManager;
    private RuleValidator ruleValidator;
    private VictoryConditionChecker victoryChecker;
    private ActiveState activeState;

    @BeforeEach
    void setUp() {
        turnManager = new TurnManager();
        ruleValidator = new RuleValidator();
        victoryChecker = new VictoryConditionChecker();
        activeState = new ActiveState(turnManager, ruleValidator, victoryChecker);
    }

    @Test
    void drawCard_removesFromDeckAndAddsToHand() {
        GameBoard board = buildBoard("p1", "p2");
        int deckSize = board.getPlayer1Board().getDeck().size();
        int handSize = board.getPlayer1Board().getHand().size();

        GameEventPublisher pub = new GameEventPublisher();
        board = turnManager.drawCard(board, pub);

        assertThat(board.getPlayer1Board().getDeck()).hasSize(deckSize - 1);
        assertThat(board.getPlayer1Board().getHand()).hasSize(handSize + 1);
    }

    @Test
    void endTurn_switchesCurrentPlayer() {
        GameBoard board = buildBoard("p1", "p2");
        board.setCurrentPlayerId("p1");
        board.setPhase(GamePhase.MAIN);

        GameEventPublisher pub = new GameEventPublisher();
        board = turnManager.endTurn(board, pub);

        assertThat(board.getCurrentPlayerId()).isEqualTo("p2");
        assertThat(board.getTurnNumber()).isEqualTo(2);
    }

    @Test
    void victoryChecker_noActivePokemon_opponentWins() {
        GameBoard board = buildBoard("p1", "p2");
        board.getPlayer1Board().setActivePokemon(null);
        board.getPlayer2Board().setActivePokemon(buildActivePokemon());
        board.setTurnNumber(2);

        String winner = victoryChecker.check(board);

        assertThat(winner).isEqualTo("p2");
    }

    @Test
    void victoryChecker_allPrizesTaken_wins() {
        GameBoard board = buildBoard("p1", "p2");
        board.getPlayer1Board().setPrizeCards(new ArrayList<>());
        board.setTurnNumber(5);

        String winner = victoryChecker.check(board);

        assertThat(winner).isEqualTo("p1");
    }

    @Test
    void ruleValidator_canAttach_whenNotAlreadyAttachedThisTurn() {
        GameBoard board = buildBoard("p1", "p2");
        board.setPhase(GamePhase.MAIN);
        board.setCurrentPlayerId("p1");
        board.getPlayer1Board().getHand().add(new EnergyCard("e1", "Grass Energy", EnergyType.GRASS));

        assertThat(ruleValidator.canAttachEnergy(board, "p1")).isTrue();
    }

    @Test
    void ruleValidator_cannotAttach_afterAlreadyAttachedThisTurn() {
        GameBoard board = buildBoard("p1", "p2");
        board.setPhase(GamePhase.MAIN);
        board.setCurrentPlayerId("p1");
        board.getPlayer1Board().setHasPlayedEnergyThisTurn(true);

        assertThat(ruleValidator.canAttachEnergy(board, "p1")).isFalse();
    }

    @Test
    void attack_dealsDamage_toOpponentActive() {
        GameBoard board = buildBoard("p1", "p2");
        board.setPhase(GamePhase.MAIN);
        board.setCurrentPlayerId("p1");

        PokemonCard attacker = buildPokemonWithAttack("Charmander", 50, "Ember", 30);
        board.getPlayer1Board().setActivePokemon(PokemonInPlay.of(attacker));

        PokemonCard defender = buildPokemon("Squirtle", 40);
        board.getPlayer2Board().setActivePokemon(PokemonInPlay.of(defender));

        GameActionMessage action = new GameActionMessage("m1", "p1", "ATTACK", Map.of("attackIndex", 0));
        GameEventPublisher pub = new GameEventPublisher();
        board = activeState.handle(ar.edu.utn.frc.tup.piii.engine.models.TurnContext.of(board, action), pub);

        assertThat(board.getPlayer2Board().getActivePokemon().getCurrentHp()).isEqualTo(10);
    }

    private GameBoard buildBoard(String p1Id, String p2Id) {
        GameBoard board = new GameBoard();
        board.setMatchId("test-match");
        board.setPlayer1Board(buildPlayerBoard(p1Id));
        board.setPlayer2Board(buildPlayerBoard(p2Id));
        board.setCurrentPlayerId(p1Id);
        board.setPhase(GamePhase.MAIN);
        board.setTurnNumber(1);
        return board;
    }

    private PlayerBoard buildPlayerBoard(String playerId) {
        PlayerBoard pb = new PlayerBoard();
        pb.setPlayerId(playerId);
        pb.setActivePokemon(buildActivePokemon());
        pb.setHand(new ArrayList<>());
        pb.setDeck(new ArrayList<>(List.of(
                new EnergyCard("e-" + playerId + "-1", "Energy", EnergyType.GRASS),
                new EnergyCard("e-" + playerId + "-2", "Energy", EnergyType.GRASS))));
        pb.setBench(new ArrayList<>());
        pb.setPrizeCards(new ArrayList<>(List.of(
                new EnergyCard("p-" + playerId + "-1", "Prize", EnergyType.COLORLESS))));
        return pb;
    }

    private PokemonInPlay buildActivePokemon() {
        return PokemonInPlay.of(buildPokemon("Bulbasaur", 60));
    }

    private PokemonCard buildPokemon(String name, int hp) {
        return new PokemonCard(name.toLowerCase(), name, hp, PokemonStage.BASIC,
                List.of(EnergyType.GRASS), new ArrayList<>(), "Fire", null, 1, null);
    }

    private PokemonCard buildPokemonWithAttack(String name, int hp, String attackName, int damage) {
        PokemonCard.Attack attack = new PokemonCard.Attack(attackName, List.of("COLORLESS"), damage, "");
        return new PokemonCard(name.toLowerCase(), name, hp, PokemonStage.BASIC,
                List.of(EnergyType.FIRE), List.of(attack), "Water", null, 1, null);
    }
}
