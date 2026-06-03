import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-deck-pile',
  standalone: true,
  templateUrl: './deck-pile.html',
  styleUrl: './deck-pile.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeckPileComponent {
  readonly count = input<number>(0);
}
