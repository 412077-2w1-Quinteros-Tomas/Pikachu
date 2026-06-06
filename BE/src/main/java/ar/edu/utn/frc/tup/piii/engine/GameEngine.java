package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.GameCard;
import ar.edu.utn.frc.tup.piii.engine.models.MatchSnapshot;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.TrainerCard;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import ar.edu.utn.frc.tup.piii.engine.state.ActiveState;
import ar.edu.utn.frc.tup.piii.engine.state.FinishedState;
import ar.edu.utn.frc.tup.piii.engine.state.MatchState;
import ar.edu.utn.frc.tup.piii.engine.state.SetupState;
import ar.edu.utn.frc.tup.piii.engine.state.WaitingState;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.repositories.DeckRepository;
import ar.edu.utn.frc.tup.piii.repositories.MatchRepository;
import ar.edu.utn.frc.tup.piii.services.GameStatePersistenceService;
import ar.edu.utn.frc.tup.piii.websocket.messages.GameActionMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngine {

    private final ObjectMapper objectMapper;
    private final GameStatePersistenceService persistenceService;
    private final MatchRepository matchRepository;
    private final DeckRepository deckRepository;

    private final WaitingState waitingState;
    private final SetupState setupState;
    private final ActiveState activeState;
    private final FinishedState finishedState;

    private static final int INITIAL_HAND_SIZE = 7;
    private static final Random RANDOM = new Random();

    @Transactional
    public MatchSnapshot initializeGame(UUID matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> EntityNotFoundException.of("Match", matchId));

        PlayerBoard p1Board = buildPlayerBoard(match.getPlayer1(), match.getDeck1Id());
        PlayerBoard p2Board = buildPlayerBoard(match.getPlayer2(), match.getDeck2Id());

        GameBoard board = new GameBoard();
        board.setMatchId(matchId.toString());
        board.setPlayer1Board(p1Board);
        board.setPlayer2Board(p2Board);
        board.setPhase(GamePhase.WAITING);
        board.setTurnNumber(0);

        String firstPlayer = RANDOM.nextBoolean() ? match.getPlayer1() : match.getPlayer2();
        board.setCurrentPlayerId(firstPlayer);
        board.log("Game initialized. First player: " + firstPlayer);

        GameEventPublisher publisher = new GameEventPublisher();
        board = setupState.enter(board, publisher);
        board = activeState.enter(board, publisher);

        saveBoard(matchId, board);
        return MatchSnapshot.of(matchId.toString(), board, publisher.drain());
    }

    @Transactional
    public MatchSnapshot processAction(UUID matchId, GameActionMessage action) {
        GameBoard board = loadBoard(matchId);
        if (board == null) {
            board = initializeGame(matchId).getBoard();
        }

        GameEventPublisher publisher = new GameEventPublisher();
        TurnContext ctx = TurnContext.of(board, action);

        MatchState state = resolveState(board.getPhase());
        board = state.handle(ctx, publisher);

        saveBoard(matchId, board);
        return MatchSnapshot.of(matchId.toString(), board, publisher.drain());
    }

    public GameBoard loadBoard(UUID matchId) {
        return persistenceService.loadState(matchId)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getStateJson(), GameBoard.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private void saveBoard(UUID matchId, GameBoard board) {
        try {
            String json = objectMapper.writeValueAsString(board);
            persistenceService.saveState(matchId, json, board.getTurnNumber(),
                    board.getCurrentPlayerId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize game board", e);
        }
    }

    private MatchState resolveState(GamePhase phase) {
        if (phase == null) return waitingState;
        return switch (phase) {
            case WAITING -> waitingState;
            case SETUP -> setupState;
            case FINISHED -> finishedState;
            default -> activeState;
        };
    }

    private PlayerBoard buildPlayerBoard(String playerId, UUID deckId) {
        PlayerBoard board = new PlayerBoard();
        board.setPlayerId(playerId);

        List<GameCard> cards = new ArrayList<>();
        if (deckId != null) {
            Optional<DeckEntity> deckOpt = deckRepository.findById(deckId);
            if (deckOpt.isPresent()) {
                for (DeckCardEntity entry : deckOpt.get().getCards()) {
                    GameCard card = toGameCard(entry.getCard());
                    if (card != null) {
                        for (int i = 0; i < entry.getQuantity(); i++) {
                            cards.add(copyCard(card, i));
                        }
                    }
                }
            }
        }

        if (cards.isEmpty()) {
            cards = buildDefaultDeck(playerId);
        }

        Collections.shuffle(cards);

        List<GameCard> prizeCards = new ArrayList<>(cards.subList(0, Math.min(PlayerBoard.PRIZE_CARD_COUNT, cards.size())));
        cards = new ArrayList<>(cards.subList(prizeCards.size(), cards.size()));

        List<GameCard> hand = new ArrayList<>(cards.subList(0, Math.min(INITIAL_HAND_SIZE, cards.size())));
        List<GameCard> deck = new ArrayList<>(cards.subList(hand.size(), cards.size()));

        board.setPrizeCards(prizeCards);
        board.setHand(hand);
        board.setDeck(deck);
        return board;
    }

    private GameCard toGameCard(CardEntity entity) {
        if (entity == null) return null;
        String id = entity.getId().toString();

        if (entity.getCardType() == CardType.POKEMON) {
            PokemonCard card = new PokemonCard();
            card.setId(id);
            card.setName(entity.getName());
            card.setHp(entity.getHp() != null ? entity.getHp() : 60);
            card.setStage(entity.getStage());
            card.setWeakness(entity.getWeakness());
            card.setResistance(entity.getResistance());
            card.setRetreatCost(entity.getRetreatCost() != null ? entity.getRetreatCost() : 1);
            card.setImageUrl(entity.getImageUrl());
            card.setAttacks(parseAttacks(entity.getAttacks()));
            card.setTypes(parseTypes(entity.getTypes()));
            return card;
        }

        if (entity.getCardType() == CardType.ENERGY) {
            EnergyCard card = new EnergyCard();
            card.setId(id);
            card.setName(entity.getName());
            card.setEnergyType(parseEnergyType(entity.getTypes()));
            return card;
        }

        TrainerCard card = new TrainerCard();
        card.setId(id);
        card.setName(entity.getName());
        card.setEffect("");
        return card;
    }

    private GameCard copyCard(GameCard card, int index) {
        if (card instanceof PokemonCard p) {
            PokemonCard copy = new PokemonCard(p.getId() + "-" + index, p.getName(), p.getHp(),
                    p.getStage(), p.getTypes(), p.getAttacks(), p.getWeakness(),
                    p.getResistance(), p.getRetreatCost(), p.getImageUrl());
            return copy;
        }
        if (card instanceof EnergyCard e) {
            return new EnergyCard(e.getId() + "-" + index, e.getName(), e.getEnergyType());
        }
        if (card instanceof TrainerCard t) {
            return new TrainerCard(t.getId() + "-" + index, t.getName(), t.getEffect());
        }
        return card;
    }

    private List<PokemonCard.Attack> parseAttacks(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            List<PokemonCard.Attack> attacks = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                String name = m.getOrDefault("name", "").toString();
                String dmgStr = m.getOrDefault("damage", "0").toString().replaceAll("[^0-9]", "");
                int dmg = dmgStr.isEmpty() ? 0 : Integer.parseInt(dmgStr);
                String effect = m.getOrDefault("effect", "") != null ? m.get("effect").toString() : "";
                Object rawCost = m.get("cost");
                @SuppressWarnings("unchecked")
                List<String> cost = rawCost instanceof List ? new ArrayList<>((List<String>) rawCost) : new ArrayList<>();
                attacks.add(new PokemonCard.Attack(name, cost, dmg, effect));
            }
            return attacks;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<EnergyType> parseTypes(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            List<String> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(s -> {
                try { return EnergyType.valueOf(s); } catch (Exception ex) { return EnergyType.COLORLESS; }
            }).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private EnergyType parseEnergyType(String json) {
        List<EnergyType> types = parseTypes(json);
        return types.isEmpty() ? EnergyType.COLORLESS : types.get(0);
    }

    private List<GameCard> buildDefaultDeck(String playerId) {
        List<GameCard> deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PokemonCard p = new PokemonCard("default-poke-" + playerId + "-" + i, "Bulbasaur",
                    60, ar.edu.utn.frc.tup.piii.enums.PokemonStage.BASIC,
                    List.of(EnergyType.GRASS),
                    List.of(new PokemonCard.Attack("Tackle", List.of("COLORLESS"), 10, "")),
                    "Fire", null, 1, null);
            deck.add(p);
        }
        for (int i = 0; i < 40; i++) {
            deck.add(new EnergyCard("default-energy-" + playerId + "-" + i, "Grass Energy", EnergyType.GRASS));
        }
        return deck;
    }
}
