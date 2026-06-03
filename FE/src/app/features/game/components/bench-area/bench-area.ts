import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { PokemonInPlay } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-bench-area',
  standalone: true,
  templateUrl: './bench-area.html',
  styleUrl: './bench-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenchAreaComponent {
  readonly bench = input<PokemonInPlay[]>([]);
  readonly energyTargetMode = input<boolean>(false);
  readonly benchSelected = output<number>();

  hpPercent(p: PokemonInPlay): number {
    return Math.max(0, (p.currentHp / p.pokemon.hp) * 100);
  }

  hpColor(p: PokemonInPlay): string {
    const pct = this.hpPercent(p);
    if (pct > 50) return '#4caf50';
    if (pct > 25) return '#ff9800';
    return '#f44336';
  }
}
