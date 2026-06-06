package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.repositories.CardRepository;
import ar.edu.utn.frc.tup.piii.repositories.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeckTemplateService {

    private static final int DECK_SIZE = 60;
    private static final String TEMPLATE_PREFIX = "🎮";

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    @Transactional
    public int seedAllTemplates() {
        int created = 0;
        created += upsertTemplate(llamasDeKalos());
        created += upsertTemplate(tormentaElectrica());
        created += upsertTemplate(profundidadesAbisales());
        created += upsertTemplate(sombrasOscuras());
        return created;
    }

    private int upsertTemplate(DeckBlueprint bp) {
        // Delete existing so trainer cards are always up to date
        deckRepository.findByName(bp.name).ifPresent(deckRepository::delete);

        List<CardEntity> allCards = cardRepository.findBySetId("xy1");

        DeckEntity deck = new DeckEntity();
        deck.setName(bp.name);
        deck.setDescription(TEMPLATE_PREFIX + " " + bp.description);
        deck = deckRepository.save(deck);

        List<DeckCardEntity> entries = new ArrayList<>();
        int usedSlots = 0;

        for (Map.Entry<String, Integer> entry : bp.recipe.entrySet()) {
            String cardName = entry.getKey();
            int qty = entry.getValue();

            CardEntity card = findCardByName(allCards, cardName);

            if (card != null) {
                DeckCardEntity dc = new DeckCardEntity();
                dc.setDeck(deck);
                dc.setCard(card);
                dc.setQuantity(qty);
                entries.add(dc);
                usedSlots += qty;
            }
        }

        int remaining = DECK_SIZE - usedSlots;
        if (remaining > 0 && bp.fillEnergyName != null) {
            CardEntity energy = allCards.stream()
                    .filter(c -> c.getCardType() == CardType.ENERGY
                            && c.getName().equalsIgnoreCase(bp.fillEnergyName))
                    .findFirst().orElse(null);

            if (energy != null) {
                DeckCardEntity dc = new DeckCardEntity();
                dc.setDeck(deck);
                dc.setCard(energy);
                dc.setQuantity(remaining);
                entries.add(dc);
            }
        }

        deck.getCards().addAll(entries);
        deckRepository.save(deck);
        return 1;
    }

    private CardEntity findCardByName(List<CardEntity> allCards, String cardName) {
        CardEntity card = allCards.stream()
                .filter(c -> c.getName().equalsIgnoreCase(cardName))
                .findFirst().orElse(null);
        if (card == null) {
            card = allCards.stream()
                    .filter(c -> c.getName().toLowerCase().contains(cardName.toLowerCase()))
                    .findFirst().orElse(null);
        }
        return card;
    }

    // ── Blueprint definitions ────────────────────────────────────────────────

    private DeckBlueprint llamasDeKalos() {
        Map<String, Integer> recipe = new LinkedHashMap<>();
        // Pokémon (20)
        recipe.put("Fennekin", 4);
        recipe.put("Braixen", 3);
        recipe.put("Delphox", 2);
        recipe.put("Slugma", 4);
        recipe.put("Magcargo", 3);
        recipe.put("Pansear", 2);
        recipe.put("Simisear", 2);
        // Entrenadores (16): solo cartas confirmadas en XY1
        recipe.put("Professor Sycamore", 4);   // Partidario: roba 7
        recipe.put("Shauna", 2);               // Partidario: baraja y roba 5
        recipe.put("Muscle Band", 4);          // Herramienta: +20 daño
        recipe.put("Super Potion", 4);         // Objeto: cura 60 HP
        recipe.put("Great Ball", 2);           // Objeto: busca Pokémon (top 7)
        // Fill: 24 Fire Energy
        return new DeckBlueprint(
                "🔥 Llamas de Kalos",
                "Mazo de Fuego con la línea evolutiva de Fennekin. ¡Quema todo a tu paso!",
                recipe,
                "Fire Energy"
        );
    }

    private DeckBlueprint tormentaElectrica() {
        Map<String, Integer> recipe = new LinkedHashMap<>();
        // Pokémon (18)
        recipe.put("Pikachu", 4);
        recipe.put("Raichu", 3);
        recipe.put("Voltorb", 4);
        recipe.put("Electrode", 3);
        recipe.put("Emolga-EX", 4);
        // Entrenadores (16): solo cartas confirmadas en XY1
        recipe.put("Professor Sycamore", 4);   // Partidario: roba 7
        recipe.put("Shauna", 2);               // Partidario: baraja y roba 5
        recipe.put("Muscle Band", 4);          // Herramienta: +20 daño
        recipe.put("Great Ball", 4);           // Objeto: busca Pokémon
        recipe.put("Evosoda", 2);              // Objeto: evoluciona desde mazo
        // Fill: 26 Lightning Energy
        return new DeckBlueprint(
                "⚡ Tormenta Eléctrica",
                "Mazo Eléctrico con Pikachu, Raichu y el poderoso Emolga-EX. ¡Descarga total!",
                recipe,
                "Lightning Energy"
        );
    }

    private DeckBlueprint profundidadesAbisales() {
        Map<String, Integer> recipe = new LinkedHashMap<>();
        // Pokémon (20)
        recipe.put("Froakie", 4);
        recipe.put("Frogadier", 3);
        recipe.put("Greninja", 2);
        recipe.put("Shellder", 4);
        recipe.put("Cloyster", 3);
        recipe.put("Staryu", 2);
        recipe.put("Starmie", 2);
        // Entrenadores (16): solo cartas confirmadas en XY1
        recipe.put("Professor Sycamore", 4);   // Partidario: roba 7
        recipe.put("Team Flare Grunt", 2);     // Partidario: descarta energía rival
        recipe.put("Muscle Band", 4);          // Herramienta: +20 daño
        recipe.put("Super Potion", 4);         // Objeto: cura 60 HP
        recipe.put("Evosoda", 2);              // Objeto: evoluciona desde mazo
        // Fill: 24 Water Energy
        return new DeckBlueprint(
                "💧 Profundidades Abisales",
                "Mazo Agua con Greninja como ninja acuático y Cloyster como barrera defensiva.",
                recipe,
                "Water Energy"
        );
    }

    private DeckBlueprint sombrasOscuras() {
        Map<String, Integer> recipe = new LinkedHashMap<>();
        // Pokémon (20)
        recipe.put("Zorua", 4);
        recipe.put("Zoroark", 3);
        recipe.put("Sandile", 4);
        recipe.put("Krokorok", 3);
        recipe.put("Krookodile", 2);
        recipe.put("Yveltal", 2);
        recipe.put("Yveltal-EX", 2);
        // Entrenadores (16): solo cartas confirmadas en XY1
        recipe.put("Professor Sycamore", 4);   // Partidario: roba 7
        recipe.put("Team Flare Grunt", 4);     // Partidario: descarta energía rival
        recipe.put("Muscle Band", 4);          // Herramienta: +20 daño
        recipe.put("Red Card", 4);             // Objeto: rival baraja y roba 4
        // Fill: 24 Darkness Energy
        return new DeckBlueprint(
                "🌑 Sombras Oscuras",
                "Mazo Oscuridad con Yveltal-EX como carta legendaria. ¡El poder de la oscuridad!",
                recipe,
                "Darkness Energy"
        );
    }

    private record DeckBlueprint(
            String name,
            String description,
            Map<String, Integer> recipe,
            String fillEnergyName
    ) {}
}
