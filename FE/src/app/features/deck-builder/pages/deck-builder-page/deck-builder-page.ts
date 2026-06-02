import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModel } from '../../../../shared/models/card.model';
import { DeckModel, CreateDeckRequest, CreateDeckCardEntry } from '../../../../shared/models/deck.model';
import { DeckService } from '../../services/deck.service';
import { CardCatalogComponent } from '../../components/card-catalog/card-catalog';
import { DeckEditorComponent, EditorEntry } from '../../components/deck-editor/deck-editor';
import { DeckListComponent } from '../../components/deck-list/deck-list';
import { DeckSummaryComponent } from '../../components/deck-summary/deck-summary';
import { CardDetailModalComponent } from '../../components/card-detail-modal/card-detail-modal';

type PageView = 'list' | 'edit';

@Component({
  selector: 'app-deck-builder-page',
  templateUrl: './deck-builder-page.html',
  styleUrl: './deck-builder-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    CardCatalogComponent,
    DeckEditorComponent,
    DeckListComponent,
    DeckSummaryComponent,
    CardDetailModalComponent,
  ],
})
export class DeckBuilderPage {

  private readonly deckService = inject(DeckService);

  readonly view = signal<PageView>('list');
  readonly decks = signal<DeckModel[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly deckName = signal('');
  readonly deckDescription = signal('');
  readonly editorEntries = signal<EditorEntry[]>([]);
  readonly editingDeckId = signal<string | null>(null);

  readonly selectedCard = signal<CardModel | null>(null);

  constructor() {
    this.loadDecks();
  }

  loadDecks(): void {
    this.loading.set(true);
    this.deckService.getAllDecks().subscribe({
      next: decks => { this.decks.set(decks); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  newDeck(): void {
    this.deckName.set('New Deck');
    this.deckDescription.set('');
    this.editorEntries.set([]);
    this.editingDeckId.set(null);
    this.view.set('edit');
  }

  editDeck(deck: DeckModel): void {
    this.deckName.set(deck.name);
    this.deckDescription.set(deck.description ?? '');
    this.editingDeckId.set(deck.id);
    const entries: EditorEntry[] = deck.cards.map(item => ({
      card: item.card,
      quantity: item.quantity,
    }));
    this.editorEntries.set(entries);
    this.view.set('edit');
  }

  deleteDeck(id: string): void {
    if (!confirm('Delete this deck?')) return;
    this.deckService.deleteDeck(id).subscribe({
      next: () => this.loadDecks(),
    });
  }

  saveDeck(): void {
    if (!this.deckName().trim()) {
      this.error.set('Deck name is required.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const request: CreateDeckRequest = {
      name: this.deckName().trim(),
      description: this.deckDescription() || undefined,
      cards: this.editorEntries().map(e => ({ cardId: e.card.id, quantity: e.quantity })),
    };

    const id = this.editingDeckId();
    const op = id
      ? this.deckService.updateDeck(id, request)
      : this.deckService.createDeck(request);

    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.view.set('list');
        this.loadDecks();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.message ?? 'Save failed.');
      },
    });
  }

  cancelEdit(): void {
    this.view.set('list');
    this.error.set(null);
  }

  onCardSelected(card: CardModel): void {
    this.addCardToEditor(card);
  }

  onCardDetailRequested(card: CardModel): void {
    this.selectedCard.set(card);
  }

  onCardDetailAdd(card: CardModel): void {
    this.addCardToEditor(card);
    this.selectedCard.set(null);
  }

  onEntriesChanged(entries: CreateDeckCardEntry[]): void {
    const currentMap = new Map(this.editorEntries().map(e => [e.card.id, e.card]));
    const updated: EditorEntry[] = entries
      .filter(e => e.quantity > 0)
      .map(e => ({
        card: currentMap.get(e.cardId)!,
        quantity: e.quantity,
      }))
      .filter(e => e.card != null);
    this.editorEntries.set(updated);
  }

  private addCardToEditor(card: CardModel): void {
    const existing = this.editorEntries().find(e => e.card.id === card.id);
    if (existing) {
      if (existing.quantity >= 4) return;
      this.editorEntries.set(
        this.editorEntries().map(e =>
          e.card.id === card.id ? { ...e, quantity: e.quantity + 1 } : e
        )
      );
    } else {
      this.editorEntries.set([...this.editorEntries(), { card, quantity: 1 }]);
    }
  }
}
