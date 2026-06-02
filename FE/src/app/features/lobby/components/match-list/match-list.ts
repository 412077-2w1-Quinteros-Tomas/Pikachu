import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatchModel } from '../../../../shared/models/match.model';

@Component({
  selector: 'app-match-list',
  templateUrl: './match-list.html',
  styleUrl: './match-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchListComponent {

  readonly matches = input<MatchModel[]>([]);
  readonly currentPlayer = input('');
  readonly matchJoined = output<MatchModel>();

  canJoin(match: MatchModel): boolean {
    return match.status === 'WAITING' && match.player1 !== this.currentPlayer();
  }

  join(match: MatchModel): void {
    this.matchJoined.emit(match);
  }

  trackById(_: number, match: MatchModel): string {
    return match.id;
  }

  statusLabel(status: string): string {
    return { WAITING: 'Esperando jugador', IN_PROGRESS: 'En juego', FINISHED: 'Finalizada' }[status] ?? status;
  }
}
