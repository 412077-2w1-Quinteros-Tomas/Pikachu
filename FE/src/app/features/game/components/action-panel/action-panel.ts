import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { EnergyCard, GameAttack, PlayerBoard } from '../../../../shared/models/game.model';
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
    if (!this.isMyTurn() || this.phase() !== 'MAIN') return false;
    if (this.board()?.hasAttackedThisTurn) return false;
    const active = this.board()?.activePokemon;
    if (!active || this.attacks.length === 0) return false;
    return this.attacks.some(a => this.hasEnoughEnergy(active.attachedEnergies ?? [], a.cost ?? []));
  }

  get canRetreat(): boolean {
    if (!this.isMyTurn() || this.phase() !== 'MAIN') return false;
    if (this.board()?.hasRetreatedThisTurn) return false;
    const active = this.board()?.activePokemon;
    if (!active || (this.board()?.bench?.length ?? 0) === 0) return false;
    return (active.attachedEnergies?.length ?? 0) >= (active.pokemon.retreatCost ?? 0);
  }

  private hasEnoughEnergy(attached: EnergyCard[], cost: string[]): boolean {
    if (!cost || cost.length === 0) return true;
    const remaining = attached.map(e => e.energyType as string);
    const colorlessNeeded: number = cost.filter(c => c.toLowerCase() === 'colorless').length;
    for (const req of cost) {
      if (req.toLowerCase() === 'colorless') continue;
      const idx = remaining.findIndex(e => e.toLowerCase() === req.toLowerCase());
      if (idx >= 0) {
        remaining.splice(idx, 1);
      } else {
        return false;
      }
    }
    return remaining.length >= colorlessNeeded;
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
