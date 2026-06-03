import { Component, ChangeDetectionStrategy, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { GameStateService } from '../../services/game-state.service';
import { GameWebsocketService } from '../../services/game-websocket.service';
import { GameBoardComponent } from '../../components/game-board/game-board';

@Component({
  selector: 'app-game-page',
  templateUrl: './game-page.html',
  styleUrl: './game-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [GameBoardComponent, RouterLink],
})
export class GamePage implements OnInit, OnDestroy {

  private readonly route = inject(ActivatedRoute);
  private readonly gameWs = inject(GameWebsocketService);
  readonly gameState = inject(GameStateService);

  matchId = '';
  playerId = '';

  ngOnInit(): void {
    this.matchId = this.route.snapshot.paramMap.get('id') ?? '';
    this.playerId = this.route.snapshot.queryParamMap.get('player') ?? '';
    if (this.matchId && this.playerId) {
      this.gameWs.connect(this.matchId, this.playerId);
    }
  }

  ngOnDestroy(): void {
    this.gameWs.disconnect();
  }
}
