package ar.edu.utn.frc.tup.piii.engine.state;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.combat.AttackResolver;
import ar.edu.utn.frc.tup.piii.engine.effects.StatusEffectManager;
import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.GameCard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.rules.TurnManager;
import ar.edu.utn.frc.tup.piii.engine.rules.VictoryConditionChecker;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActiveState implements MatchState {

    private final TurnManager turnManager;
    private final RuleValidator ruleValidator;
    private final VictoryConditionChecker victoryConditionChecker;
    private final AttackResolver attackResolver;
    private final StatusEffectManager statusEffectManager;

    @Override
    public GameBoard enter(GameBoard board, GameEventPublisher publisher) {
        board.setPhase(GamePhase.DRAW);
        board.setTurnNumber(1);
        publisher.publish(GameEvent.of(GameEventType.TURN_STARTED, board.getCurrentPlayerId(),
                Map.of("turnNumber", board.getTurnNumber())));
        return turnManager.drawCard(board, publisher);
    }

    @Override
    public GameBoard handle(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String actionType = ctx.getAction() != null ? ctx.getAction().getActionType() : "";
        String actingPlayer = ctx.getAction() != null ? ctx.getAction().getPlayerId() : "";

        if (!actingPlayer.equals(board.getCurrentPlayerId())) {
            board.log("Action ignored: not " + actingPlayer + "'s turn");
            return board;
        }

        switch (actionType) {
            case "PLAY_CARD" -> board = handlePlayCard(ctx, publisher);
            case "ATTACH_ENERGY" -> board = handleAttachEnergy(ctx, publisher);
            case "ATTACK" -> board = handleAttack(ctx, publisher);
            case "RETREAT" -> board = handleRetreat(ctx, publisher);
            case "END_TURN" -> board = doEndTurn(board, publisher);
            default -> board.log("Unknown action: " + actionType);
        }

        String winner = victoryConditionChecker.check(board);
        if (winner != null) {
            board.setWinnerId(winner);
            board.setPhase(GamePhase.FINISHED);
            publisher.publish(GameEvent.of(GameEventType.GAME_OVER, winner,
                    Map.of("winner", winner)));
        }

        return board;
    }

    private GameBoard handlePlayCard(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String playerId = ctx.getAction().getPlayerId();
        PlayerBoard pb = board.getBoardFor(playerId);
        if (pb == null) return board;

        Object cardIdObj = ctx.getAction().getPayload() != null
                ? ctx.getAction().getPayload().get("cardId") : null;
        if (cardIdObj == null) return board;
        String cardId = cardIdObj.toString();

        GameCard card = pb.getHand().stream()
                .filter(c -> cardId.equals(c.getId()))
                .findFirst().orElse(null);
        if (card == null) {
            board.log("Card " + cardId + " not found in hand");
            return board;
        }

        if (card.getCardType() == CardType.POKEMON) {
            PokemonCard pokemon = (PokemonCard) card;
            if (pokemon.getStage() == PokemonStage.BASIC) {
                if (pb.getActivePokemon() == null) {
                    pb.setActivePokemon(PokemonInPlay.of(pokemon));
                    pb.getHand().remove(card);
                    publisher.publish(GameEvent.of(GameEventType.POKEMON_PLACED, playerId,
                            Map.of("pokemon", pokemon.getName(), "zone", "active")));
                } else if (pb.hasBenchSpace()) {
                    pb.getBench().add(PokemonInPlay.of(pokemon));
                    pb.getHand().remove(card);
                    publisher.publish(GameEvent.of(GameEventType.POKEMON_PLACED, playerId,
                            Map.of("pokemon", pokemon.getName(), "zone", "bench")));
                }
            }
        }

        return board;
    }

    private GameBoard handleAttachEnergy(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String playerId = ctx.getAction().getPlayerId();
        PlayerBoard pb = board.getBoardFor(playerId);
        if (pb == null || pb.isHasPlayedEnergyThisTurn()) return board;

        if (ctx.getAction().getPayload() == null) return board;
        String energyCardId = ctx.getAction().getPayload().getOrDefault("energyCardId", "").toString();
        String targetInstanceId = ctx.getAction().getPayload().getOrDefault("targetInstanceId", "").toString();

        EnergyCard energy = pb.getHand().stream()
                .filter(c -> c instanceof EnergyCard && energyCardId.equals(c.getId()))
                .map(c -> (EnergyCard) c)
                .findFirst().orElse(null);
        if (energy == null) return board;

        PokemonInPlay target = findInPlay(pb, targetInstanceId);
        if (target == null) return board;

        pb.getHand().remove(energy);
        target.getAttachedEnergies().add(energy);
        pb.setHasPlayedEnergyThisTurn(true);

        publisher.publish(GameEvent.of(GameEventType.ENERGY_ATTACHED, playerId,
                Map.of("energy", energy.getName(), "target", target.getPokemon().getName())));

        return board;
    }

    private GameBoard handleAttack(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String playerId = ctx.getAction().getPlayerId();
        PlayerBoard attacker = board.getBoardFor(playerId);

        if (attacker == null || attacker.getActivePokemon() == null) return board;
        if (attacker.isHasAttackedThisTurn()) {
            board.log(playerId + " already attacked this turn");
            return board;
        }
        if (board.getTurnNumber() == 1) {
            board.log("First player cannot attack on their first turn");
            return board;
        }

        if (!statusEffectManager.canAttack(attacker.getActivePokemon())) {
            board.log(attacker.getActivePokemon().getPokemon().getName() + " cannot attack due to a status condition");
            statusEffectManager.applyBetweenTurnEffects(attacker, publisher);
            return doEndTurn(board, publisher);
        }

        int attackIndex = 0;
        if (ctx.getAction().getPayload() != null) {
            Object idx = ctx.getAction().getPayload().get("attackIndex");
            if (idx != null) attackIndex = Integer.parseInt(idx.toString());
        }

        publisher.publish(GameEvent.of(GameEventType.ATTACK_PERFORMED, playerId,
                Map.of("attacker", attacker.getActivePokemon().getPokemon().getName(),
                        "attackIndex", attackIndex)));

        AttackContext attackCtx = attackResolver.resolve(board, playerId, attackIndex, publisher);
        board = attackCtx.getBoard();

        attacker.setHasAttackedThisTurn(true);

        if (!attackCtx.isCancelled()) {
            PlayerBoard defender = board.getOpponentBoard(playerId);
            if (defender != null && defender.getActivePokemon() != null
                    && defender.getActivePokemon().isKnockedOut()) {
                handleKnockOut(board, playerId, defender, publisher);
            }
        }

        board.setPhase(GamePhase.BETWEEN_TURNS);
        return doEndTurn(board, publisher);
    }

    private void handleKnockOut(GameBoard board, String attackerId,
                                 PlayerBoard defender, GameEventPublisher publisher) {
        PokemonInPlay koedPokemon = defender.getActivePokemon();
        publisher.publish(GameEvent.of(GameEventType.POKEMON_KO, defender.getPlayerId(),
                Map.of("pokemon", koedPokemon.getPokemon().getName())));

        defender.getDiscardPile().add(koedPokemon.getPokemon());
        if (koedPokemon.getAttachedEnergies() != null) {
            defender.getDiscardPile().addAll(koedPokemon.getAttachedEnergies());
        }
        defender.setActivePokemon(null);

        PlayerBoard attackerBoard = board.getBoardFor(attackerId);
        if (attackerBoard != null && !attackerBoard.getPrizeCards().isEmpty()) {
            GameCard prize = attackerBoard.getPrizeCards().remove(0);
            attackerBoard.getHand().add(prize);
            publisher.publish(GameEvent.of(GameEventType.PRIZE_TAKEN, attackerId,
                    Map.of("prizeCardsLeft", attackerBoard.getPrizeCards().size())));
        }

        if (!defender.getBench().isEmpty()) {
            PokemonInPlay newActive = defender.getBench().remove(0);
            defender.setActivePokemon(newActive);
            board.log(defender.getPlayerId() + " sent out " + newActive.getPokemon().getName());
        }
    }

    private GameBoard handleRetreat(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String playerId = ctx.getAction().getPlayerId();
        PlayerBoard pb = board.getBoardFor(playerId);

        if (pb == null || pb.isHasRetreatedThisTurn()
                || pb.getActivePokemon() == null || pb.getBench().isEmpty()) return board;

        if (!statusEffectManager.canRetreat(pb.getActivePokemon())) {
            board.log(pb.getActivePokemon().getPokemon().getName() + " cannot retreat due to a status condition");
            return board;
        }

        Object targetObj = ctx.getAction().getPayload() != null
                ? ctx.getAction().getPayload().get("benchIndex") : null;
        int benchIndex = targetObj != null ? Integer.parseInt(targetObj.toString()) : 0;

        if (benchIndex >= pb.getBench().size()) return board;

        PokemonInPlay active = pb.getActivePokemon();
        int retreatCost = active.getPokemon().getRetreatCost();

        if (active.getAttachedEnergies().size() < retreatCost) {
            board.log(active.getPokemon().getName() + " needs " + retreatCost + " energy to retreat (has "
                    + active.getAttachedEnergies().size() + ")");
            return board;
        }

        for (int i = 0; i < retreatCost; i++) {
            EnergyCard discarded = active.getAttachedEnergies().remove(0);
            pb.getDiscardPile().add(discarded);
        }

        active.setSpecialCondition(null);

        PokemonInPlay newActive = pb.getBench().remove(benchIndex);
        pb.getBench().add(0, active);
        pb.setActivePokemon(newActive);
        pb.setHasRetreatedThisTurn(true);

        publisher.publish(GameEvent.of(GameEventType.PLAYER_RETREATED, playerId,
                Map.of("newActive", newActive.getPokemon().getName())));

        return board;
    }

    private GameBoard doEndTurn(GameBoard board, GameEventPublisher publisher) {
        String currentPlayer = board.getCurrentPlayerId();
        PlayerBoard currentBoard = board.getBoardFor(currentPlayer);
        PlayerBoard opponentBoard = board.getOpponentBoard(currentPlayer);
        if (currentBoard != null) {
            statusEffectManager.applyBetweenTurnEffects(currentBoard, publisher);
        }
        if (opponentBoard != null) {
            statusEffectManager.applyBetweenTurnEffects(opponentBoard, publisher);
        }
        return turnManager.endTurn(board, publisher);
    }

    private PokemonInPlay findInPlay(PlayerBoard pb, String instanceId) {
        if (pb.getActivePokemon() != null && instanceId.equals(pb.getActivePokemon().getInstanceId())) {
            return pb.getActivePokemon();
        }
        return pb.getBench().stream()
                .filter(p -> instanceId.equals(p.getInstanceId()))
                .findFirst().orElse(null);
    }

    @Override
    public String getStateName() {
        return "ACTIVE";
    }
}
