import { Component, ChangeDetectionStrategy, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { GameStateService } from '../../services/game-state.service';
import { GameWebsocketService } from '../../services/game-websocket.service';

@Component({
  selector: 'app-game-page',
  templateUrl: './game-page.html',
  styleUrl: './game-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
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

  endTurn(): void {
    this.gameWs.sendAction(this.matchId, this.playerId, 'END_TURN');
  }
}
