import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { GameCard } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-prize-cards',
  standalone: true,
  templateUrl: './prize-cards.html',
  styleUrl: './prize-cards.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrizeCardsComponent {
  readonly prizes = input<GameCard[]>([]);
  readonly isOpponent = input<boolean>(false);
}
