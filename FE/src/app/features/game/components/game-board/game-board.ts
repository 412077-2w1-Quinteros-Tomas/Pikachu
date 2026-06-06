import { Component, ChangeDetectionStrategy, input, inject, computed, signal } from '@angular/core';
import { GameStateService } from '../../services/game-state.service';
import { GameWebsocketService } from '../../services/game-websocket.service';
import { GameStateMachineService } from '../../services/game-state-machine.service';
import { PokemonInPlay } from '../../../../shared/models/game.model';
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
  protected readonly stateMachine = inject(GameStateMachineService);
  private readonly gameWs = inject(GameWebsocketService);

  protected readonly validationError = signal<string | null>(null);

  protected readonly selectedEnergy = signal<string | null>(null);

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

  protected readonly isEnergyMode = computed(() => this.selectedEnergy() !== null);

  onEnergyCardSelected(energyCardId: string): void {
    this.selectedEnergy.set(energyCardId);
  }

  onEnergyTargetSelected(instanceId: string): void {
    const energyCardId = this.selectedEnergy();
    if (!energyCardId) return;
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'ATTACH_ENERGY',
      { energyCardId, targetInstanceId: instanceId });
    this.selectedEnergy.set(null);
  }

  cancelEnergyMode(): void {
    this.selectedEnergy.set(null);
  }

  onAttack(attackIndex: number): void {
    const check = this.stateMachine.validate({ type: 'ATTACK', attackIndex });
    if (!check.valid) { this.showValidationError(check.reason!); return; }
    this.clearValidationError();
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'ATTACK', { attackIndex });
  }

  onEndTurn(): void {
    this.selectedEnergy.set(null);
    this.clearValidationError();
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'END_TURN');
  }

  onRetreat(benchIndex: number): void {
    if (this.isEnergyMode()) { this.cancelEnergyMode(); return; }
    const check = this.stateMachine.validate({ type: 'RETREAT', benchIndex });
    if (!check.valid) { this.showValidationError(check.reason!); return; }
    this.clearValidationError();
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'RETREAT', { benchIndex });
  }

  onPlayCard(cardId: string): void {
    this.clearValidationError();
    this.gameWs.sendAction(this.matchId(), this.playerId(), 'PLAY_CARD', { cardId });
  }

  private showValidationError(reason: string): void {
    this.validationError.set(reason);
    setTimeout(() => this.validationError.set(null), 3000);
  }

  private clearValidationError(): void {
    this.validationError.set(null);
  }

  onActiveTargetSelected(): void {
    const b = this.gameState.myBoard();
    if (b?.activePokemon) {
      this.onEnergyTargetSelected(b.activePokemon.instanceId);
    }
  }

  onBenchTargetSelected(index: number): void {
    const b = this.gameState.myBoard();
    if (b?.bench[index]) {
      this.onEnergyTargetSelected(b.bench[index].instanceId);
    }
  }
}
