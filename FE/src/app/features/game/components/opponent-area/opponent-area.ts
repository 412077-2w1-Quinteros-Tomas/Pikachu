import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { PlayerBoard } from '../../../../shared/models/game.model';
import { ActivePokemonComponent } from '../active-pokemon/active-pokemon';
import { BenchAreaComponent } from '../bench-area/bench-area';
import { PrizeCardsComponent } from '../prize-cards/prize-cards';
import { DeckPileComponent } from '../deck-pile/deck-pile';
import { DiscardPileComponent } from '../discard-pile/discard-pile';

@Component({
  selector: 'app-opponent-area',
  standalone: true,
  templateUrl: './opponent-area.html',
  styleUrl: './opponent-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ActivePokemonComponent, BenchAreaComponent, PrizeCardsComponent, DeckPileComponent, DiscardPileComponent]
})
export class OpponentAreaComponent {
  readonly board = input<PlayerBoard | null>(null);
}
