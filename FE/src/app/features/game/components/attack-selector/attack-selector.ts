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
}
