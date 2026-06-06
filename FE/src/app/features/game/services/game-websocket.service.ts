import { Injectable, inject, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../../../core/services/websocket.service';
import { GameStateService } from './game-state.service';
import { GameStateMachineService } from './game-state-machine.service';
import { GameActionMessage, GameStateUpdateMessage } from '../../../shared/models/match.model';
import { MatchSnapshot } from '../../../shared/models/game.model';

@Injectable({ providedIn: 'root' })
export class GameWebsocketService implements OnDestroy {

  private readonly ws = inject(WebsocketService);
  private readonly gameState = inject(GameStateService);
  private readonly stateMachine = inject(GameStateMachineService);
  private sub: Subscription | null = null;

  connect(matchId: string, playerId: string): void {
    this.gameState.setPlayerId(playerId);
    this.stateMachine.setPlayerId(playerId);
    this.sub = this.ws.connect(matchId, playerId).subscribe({
      next: (msg: GameStateUpdateMessage) => this.handleMessage(msg),
    });
    this.gameState.setConnected(true);
  }

  sendAction(matchId: string, playerId: string, actionType: string,
             payload?: Record<string, unknown>): void {
    const msg: GameActionMessage = { matchId, playerId, actionType, payload };
    this.ws.send(msg);
  }

  disconnect(): void {
    this.sub?.unsubscribe();
    this.ws.disconnect();
    this.gameState.setConnected(false);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private handleMessage(msg: GameStateUpdateMessage): void {
    switch (msg.type) {
      case 'GAME_STARTED':
      case 'STATE_UPDATE': {
        if (msg.data) {
          const snap = msg.data as MatchSnapshot;
          const prevPhase = this.stateMachine.phase();
          this.gameState.applySnapshot(snap);
          this.stateMachine.applyServerBoard(snap.board);
          // Reset constraints when a new turn starts
          if (prevPhase !== snap.board.phase || snap.board.phase === 'DRAW') {
            this.stateMachine.onTurnStart();
          }
        }
        break;
      }
      case 'PLAYER_CONNECTED':
        this.gameState.setConnected(true);
        break;
      case 'PLAYER_DISCONNECTED':
        this.gameState.setError('El oponente se desconectó.');
        break;
      case 'ERROR':
        this.gameState.setError(
          (msg.data as Record<string, string>)?.['message'] ?? 'Error desconocido'
        );
        break;
    }
  }
}
