import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { PlayerBoard, PokemonInPlay } from '../../../../shared/models/game.model';
import { ActivePokemonComponent } from '../active-pokemon/active-pokemon';
import { BenchAreaComponent } from '../bench-area/bench-area';
import { PrizeCardsComponent } from '../prize-cards/prize-cards';
import { DeckPileComponent } from '../deck-pile/deck-pile';
import { DiscardPileComponent } from '../discard-pile/discard-pile';

@Component({
  selector: 'app-player-area',
  standalone: true,
  templateUrl: './player-area.html',
  styleUrl: './player-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ActivePokemonComponent, BenchAreaComponent, PrizeCardsComponent, DeckPileComponent, DiscardPileComponent]
})
export class PlayerAreaComponent {
  readonly board = input<PlayerBoard | null>(null);
  readonly isMyTurn = input<boolean>(false);

  readonly benchSelected = output<number>();

  get allTargets(): PokemonInPlay[] {
    const b = this.board();
    if (!b) return [];
    const list: PokemonInPlay[] = [];
    if (b.activePokemon) list.push(b.activePokemon);
    list.push(...(b.bench ?? []));
    return list;
  }
}
