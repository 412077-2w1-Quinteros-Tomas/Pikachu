import { Component, ChangeDetectionStrategy, input, inject, computed } from '@angular/core';
import { GameStateService } from '../../services/game-state.service';
import { GameWebsocketService } from '../../services/game-websocket.service';
import { EnergyCard, PokemonInPlay } from '../../../../shared/models/game.model';
import { OpponentAreaComponent } from '../opponent-area/opponent-area';
import { PlayerAreaComponent } from '../player-area/player-area';
import { HandAreaComponent } from '../hand-area/hand-area';
import { ActionPanelComponent } from '../action-panel/action-panel';
import { NotificationToastComponent } from '../notification-toast/notification-toast';
import { SetupPhaseComponent } from '../setup-phase/setup-phase';
import { GameOverComponent } from '../game-over/game-over';

@Component({
  selector: 'app-game-board',
  standalone: true,
  templateUrl: './game-board.html',
  styleUrl: './game-board.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    OpponentAreaComponent, PlayerAreaComponent, HandAreaComponent,
    ActionPanelComponent, NotificationToastComponent,
    SetupPhaseComponent, GameOverComponent
  ]
})
export class GameBoardComponent {
  readonly matchId = input.required<string>();
  readonly playerId = input.required<string>();

  protected readonly gameState = inject(GameStateService);
  private readonly gameWs = inject(GameWebsocketService);

  protected readonly allTargets = computed<PokemonInPlay[]>(() => {
    const b = this.gameState.myBoard();
    if (!b) return [];
    const list: PokemonInPlay[] = [];
    if (b.activePokemon) list.push(b.activePokemon);
    list.push(...(b.bench ?? []));
    return list;
  });

  protected readonly canPlayEnergy = computed<boolean>(() =>
    this.gameState.isMyTurn() && !(this.gameState.myBoard()?.hasPlayedEnergyThisTurn ?? true)
  );

  onAttack(attackIndex: number): void {
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'ATTACK', { attackIndex });
  }

  onEndTurn(): void {
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'END_TURN');
  }

  onRetreat(benchIndex: number): void {
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'RETREAT', { benchIndex });
  }

  onPlayCard(cardId: string): void {
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'PLAY_CARD', { cardId });
  }

  onAttachEnergy(event: { energyCardId: string; targetInstanceId: string }): void {
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'ATTACH_ENERGY', event);
  }
}
