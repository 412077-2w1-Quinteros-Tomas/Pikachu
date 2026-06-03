import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-game-over',
  standalone: true,
  templateUrl: './game-over.html',
  styleUrl: './game-over.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink]
})
export class GameOverComponent {
  readonly winner = input<string | null>(null);
  readonly myPlayerId = input<string>('');

  get didWin(): boolean {
    return this.winner() === this.myPlayerId();
  }
}
