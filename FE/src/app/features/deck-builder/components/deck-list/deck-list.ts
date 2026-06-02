import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { DeckModel } from '../../../../shared/models/deck.model';

@Component({
  selector: 'app-deck-list',
  templateUrl: './deck-list.html',
  styleUrl: './deck-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckListComponent {

  readonly decks = input<DeckModel[]>([]);
  readonly deckSelected = output<DeckModel>();
  readonly deckDeleted = output<string>();

  select(deck: DeckModel): void {
    this.deckSelected.emit(deck);
  }

  delete(deck: DeckModel, event: Event): void {
    event.stopPropagation();
    this.deckDeleted.emit(deck.id);
  }

  trackById(_: number, deck: DeckModel): string {
    return deck.id;
  }
}
