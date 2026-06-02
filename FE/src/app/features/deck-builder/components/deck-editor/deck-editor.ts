import { Component, ChangeDetectionStrategy, input, output, computed, signal } from '@angular/core';
import { CardModel } from '../../../../shared/models/card.model';
import { CreateDeckCardEntry } from '../../../../shared/models/deck.model';

export interface EditorEntry {
  card: CardModel;
  quantity: number;
}

@Component({
  selector: 'app-deck-editor',
  templateUrl: './deck-editor.html',
  styleUrl: './deck-editor.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckEditorComponent {

  readonly entries = input<EditorEntry[]>([]);
  readonly entryChanged = output<CreateDeckCardEntry[]>();
  readonly cardDetailRequested = output<CardModel>();

  readonly MAX_COPIES = 4;
  readonly DECK_SIZE = 60;

  readonly totalCards = computed(() =>
    this.entries().reduce((sum, e) => sum + e.quantity, 0)
  );

  readonly isComplete = computed(() => this.totalCards() === this.DECK_SIZE);

  addOne(card: CardModel): void {
    const current = this.entries().find(e => e.card.id === card.id);
    const currentQty = current?.quantity ?? 0;
    if (currentQty >= this.MAX_COPIES) return;
    this.emit(card.id, currentQty + 1);
  }

  removeOne(card: CardModel): void {
    const current = this.entries().find(e => e.card.id === card.id);
    if (!current) return;
    this.emit(card.id, current.quantity - 1);
  }

  setQuantity(card: CardModel, qty: number): void {
    this.emit(card.id, Math.max(0, Math.min(qty, this.MAX_COPIES)));
  }

  showDetail(card: CardModel): void {
    this.cardDetailRequested.emit(card);
  }

  trackByCardId(_: number, entry: EditorEntry): string {
    return entry.card.id;
  }

  private emit(cardId: string, newQty: number): void {
    const current = this.entries().filter(e => e.card.id !== cardId);
    const updated: CreateDeckCardEntry[] = [
      ...current.map(e => ({ cardId: e.card.id, quantity: e.quantity })),
      ...(newQty > 0 ? [{ cardId, quantity: newQty }] : []),
    ];
    this.entryChanged.emit(updated);
  }
}
