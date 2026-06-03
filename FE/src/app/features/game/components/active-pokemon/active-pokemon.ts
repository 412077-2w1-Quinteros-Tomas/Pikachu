import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { PokemonInPlay } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-active-pokemon',
  standalone: true,
  templateUrl: './active-pokemon.html',
  styleUrl: './active-pokemon.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivePokemonComponent {
  readonly pokemon = input<PokemonInPlay | null>(null);
  readonly label = input<string>('Activo');
  readonly clickable = input<boolean>(false);
  readonly selected = output<void>();

  get hpPercent(): number {
    const p = this.pokemon();
    if (!p) return 100;
    return Math.max(0, (p.currentHp / p.pokemon.hp) * 100);
  }

  get hpColor(): string {
    const pct = this.hpPercent;
    if (pct > 50) return '#4caf50';
    if (pct > 25) return '#ff9800';
    return '#f44336';
  }

  get conditionLabel(): string {
    const cond = this.pokemon()?.specialCondition;
    if (!cond) return '';
    const labels: Record<string, string> = {
      ASLEEP: '💤', BURNED: '🔥', CONFUSED: '😵', PARALYZED: '⚡', POISONED: '☠️'
    };
    return labels[cond] ?? cond;
  }
}
