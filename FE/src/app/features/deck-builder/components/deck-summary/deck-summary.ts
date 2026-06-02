import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { EditorEntry } from '../deck-editor/deck-editor';
import { CardType } from '../../../../shared/models/card.model';

interface TypeCount { type: CardType; label: string; count: number; color: string; }
interface ValidationRule { label: string; passed: boolean; }

@Component({
  selector: 'app-deck-summary',
  templateUrl: './deck-summary.html',
  styleUrl: './deck-summary.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckSummaryComponent {

  readonly entries = input<EditorEntry[]>([]);
  readonly deckName = input('');
  readonly DECK_SIZE = 60;

  readonly totalCards = computed(() =>
    this.entries().reduce((sum, e) => sum + e.quantity, 0)
  );

  readonly progress = computed(() =>
    Math.min(100, (this.totalCards() / this.DECK_SIZE) * 100)
  );

  readonly hasBasic = computed(() =>
    this.entries().some(e => e.card.cardType === 'POKEMON' && e.card.stage === 'BASIC' && e.quantity > 0)
  );

  readonly exceedsLimit = computed(() =>
    this.entries().filter(e => e.card.cardType !== 'ENERGY').some(e => e.quantity > 4)
  );

  readonly isComplete = computed(() => this.totalCards() === this.DECK_SIZE);

  readonly isValid = computed(() =>
    this.isComplete() && this.hasBasic() && !this.exceedsLimit()
  );

  readonly rules = computed<ValidationRule[]>(() => [
    { label: `${this.totalCards()} / 60 cartas exactas`, passed: this.isComplete() },
    { label: 'Al menos 1 Pokémon Basic', passed: this.hasBasic() },
    { label: 'Máx. 4 copias por carta', passed: !this.exceedsLimit() },
  ]);

  readonly typeCounts = computed<TypeCount[]>(() => {
    const map: Record<string, { label: string; color: string; count: number }> = {
      POKEMON:  { label: 'Pokémon',    color: '#66BB6A', count: 0 },
      ENERGY:   { label: 'Energía',    color: '#FFD54F', count: 0 },
      TRAINER:  { label: 'Entrenador', color: '#4FC3F7', count: 0 },
    };
    for (const e of this.entries()) {
      if (map[e.card.cardType]) map[e.card.cardType].count += e.quantity;
    }
    return Object.entries(map)
      .filter(([, v]) => v.count > 0)
      .map(([type, v]) => ({ type: type as CardType, ...v }));
  });
}
