import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { GameAttack } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-attack-selector',
  standalone: true,
  templateUrl: './attack-selector.html',
  styleUrl: './attack-selector.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AttackSelectorComponent {
  readonly attacks = input<GameAttack[]>([]);
  readonly attackSelected = output<number>();
  readonly cancelled = output<void>();

  private static readonly ENERGY_COLORS: Record<string, string> = {
    FIRE:'#ff5500', WATER:'#0088ff', GRASS:'#33cc00',
    LIGHTNING:'#ffdd00', PSYCHIC:'#cc44ff', FIGHTING:'#cc6600',
    DARKNESS:'#5566bb', METAL:'#99aacc', FAIRY:'#ff66aa',
    DRAGON:'#7755dd', COLORLESS:'#aaaaaa'
  };

  energyColor(type: string): string {
    return AttackSelectorComponent.ENERGY_COLORS[type] ?? '#888';
  }
}
