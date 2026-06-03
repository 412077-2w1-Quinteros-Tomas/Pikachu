import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { GameAttack, PlayerBoard } from '../../../../shared/models/game.model';
import { AttackSelectorComponent } from '../attack-selector/attack-selector';

@Component({
  selector: 'app-action-panel',
  standalone: true,
  templateUrl: './action-panel.html',
  styleUrl: './action-panel.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AttackSelectorComponent]
})
export class ActionPanelComponent {
  readonly board = input<PlayerBoard | null>(null);
  readonly isMyTurn = input<boolean>(false);
  readonly phase = input<string | null>(null);

  readonly attack = output<number>();
  readonly endTurn = output<void>();
  readonly retreat = output<number>();

  readonly showAttackSelector = signal(false);
  readonly showRetreatPicker = signal(false);

  get attacks(): GameAttack[] {
    return this.board()?.activePokemon?.pokemon.attacks ?? [];
  }

  get canAttack(): boolean {
    return this.isMyTurn()
      && (this.phase() === 'MAIN' || this.phase() === 'DRAW')
      && !this.board()?.hasAttackedThisTurn
      && !!this.board()?.activePokemon
      && this.attacks.length > 0;
  }

  get canRetreat(): boolean {
    return this.isMyTurn()
      && (this.phase() === 'MAIN' || this.phase() === 'DRAW')
      && !this.board()?.hasRetreatedThisTurn
      && !!this.board()?.activePokemon
      && (this.board()?.bench?.length ?? 0) > 0;
  }

  get canEndTurn(): boolean {
    return this.isMyTurn() && !!this.phase();
  }

  onAttackSelected(index: number): void {
    this.showAttackSelector.set(false);
    this.attack.emit(index);
  }

  onRetreatSelected(index: number): void {
    this.showRetreatPicker.set(false);
    this.retreat.emit(index);
  }
}
