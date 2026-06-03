package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import ar.edu.utn.frc.tup.piii.engine.state.FinishedState;
import ar.edu.utn.frc.tup.piii.engine.state.SetupState;
import ar.edu.utn.frc.tup.piii.engine.state.WaitingState;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EngineStateTest {

    @Test
    void waitingState_enter_setsWaitingPhase() {
        WaitingState state = new WaitingState();
        GameBoard board = buildBoard("p1", "p2");
        GameEventPublisher pub = new GameEventPublisher();

        GameBoard result = state.enter(board, pub);

        assertThat(result.getPhase()).isEqualTo(GamePhase.WAITING);
    }

    @Test
    void waitingState_handle_returnsUnchanged() {
        WaitingState state = new WaitingState();
        GameBoard board = buildBoard("p1", "p2");
        TurnContext ctx = TurnContext.of(board, null);

        GameBoard result = state.handle(ctx, new GameEventPublisher());

        assertThat(result).isSameAs(board);
    }

    @Test
    void waitingState_stateName() {
        assertThat(new WaitingState().getStateName()).isEqualTo("WAITING");
    }

    @Test
    void setupState_enter_placesBasicPokemonAsActive() {
        SetupState state = new SetupState();
        GameBoard board = buildBoard("p1", "p2");
        PokemonCard pokemon = buildPokemon("Bulbasaur", 60);
        board.getPlayer1Board().getHand().add(pokemon);
        board.getPlayer1Board().setActivePokemon(null);

        GameEventPublisher pub = new GameEventPublisher();
        GameBoard result = state.enter(board, pub);

        assertThat(result.getPlayer1Board().getActivePokemon()).isNotNull();
        assertThat(result.getPlayer1Board().getActivePokemon().getPokemon().getName()).isEqualTo("Bulbasaur");
        assertThat(result.getPhase()).isEqualTo(GamePhase.SETUP);
    }

    @Test
    void setupState_enter_noBasicPokemon_leavesActiveNull() {
        SetupState state = new SetupState();
        GameBoard board = buildBoard("p1", "p2");
        board.getPlayer1Board().setActivePokemon(null);
        board.getPlayer1Board().getHand().clear();

        state.enter(board, new GameEventPublisher());

        assertThat(board.getPlayer1Board().getActivePokemon()).isNull();
    }

    @Test
    void setupState_handle_returnsBoard() {
        SetupState state = new SetupState();
        GameBoard board = buildBoard("p1", "p2");
        TurnContext ctx = TurnContext.of(board, null);

        GameBoard result = state.handle(ctx, new GameEventPublisher());

        assertThat(result).isSameAs(board);
    }

    @Test
    void setupState_stateName() {
        assertThat(new SetupState().getStateName()).isEqualTo("SETUP");
    }

    @Test
    void finishedState_enter_returnsBoard() {
        FinishedState state = new FinishedState();
        GameBoard board = buildBoard("p1", "p2");

        GameBoard result = state.enter(board, new GameEventPublisher());

        assertThat(result).isSameAs(board);
    }

    @Test
    void finishedState_handle_returnsUnchanged() {
        FinishedState state = new FinishedState();
        GameBoard board = buildBoard("p1", "p2");
        TurnContext ctx = TurnContext.of(board, null);

        GameBoard result = state.handle(ctx, new GameEventPublisher());

        assertThat(result).isSameAs(board);
    }

    @Test
    void finishedState_stateName() {
        assertThat(new FinishedState().getStateName()).isEqualTo("FINISHED");
    }

    private GameBoard buildBoard(String p1Id, String p2Id) {
        GameBoard board = new GameBoard();
        board.setMatchId("test-match");
        board.setPlayer1Board(buildPlayerBoard(p1Id));
        board.setPlayer2Board(buildPlayerBoard(p2Id));
        board.setCurrentPlayerId(p1Id);
        board.setPhase(GamePhase.WAITING);
        board.setTurnNumber(0);
        return board;
    }

    private PlayerBoard buildPlayerBoard(String playerId) {
        PlayerBoard pb = new PlayerBoard();
        pb.setPlayerId(playerId);
        pb.setHand(new ArrayList<>());
        pb.setDeck(new ArrayList<>(List.of(
                new EnergyCard("e-" + playerId, "Energy", EnergyType.GRASS))));
        pb.setBench(new ArrayList<>());
        pb.setPrizeCards(new ArrayList<>());
        pb.setActivePokemon(PokemonInPlay.of(buildPokemon("Bulbasaur", 60)));
        return pb;
    }

    private PokemonCard buildPokemon(String name, int hp) {
        return new PokemonCard(name.toLowerCase(), name, hp, PokemonStage.BASIC,
                List.of(EnergyType.GRASS), new ArrayList<>(), null, null, 1, null);
    }
}
