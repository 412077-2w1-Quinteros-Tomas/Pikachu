import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { PokemonInPlay } from '../../../../shared/models/game.model';

const ENERGY_COLORS: Record<string, string> = {
  FIRE: '#ff5533', WATER: '#3388ff', GRASS: '#44bb44',
  LIGHTNING: '#ffdd00', PSYCHIC: '#cc44cc', FIGHTING: '#cc6600',
  DARKNESS: '#334466', METAL: '#999999', FAIRY: '#ff88bb',
  DRAGON: '#7755cc', COLORLESS: '#aaaaaa'
};

const ENERGY_ICONS: Record<string, string> = {
  FIRE: '🔥', WATER: '💧', GRASS: '🌿', LIGHTNING: '⚡',
  PSYCHIC: '🔮', FIGHTING: '👊', DARKNESS: '🌑', METAL: '⚙️',
  FAIRY: '✨', DRAGON: '🐉', COLORLESS: '⭐'
};

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
  readonly energyTargetMode = input<boolean>(false);
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
    return { ASLEEP: '💤 Dormido', BURNED: '🔥 Quemado', CONFUSED: '😵 Confundido',
             PARALYZED: '⚡ Paralizado', POISONED: '☠️ Envenenado' }[cond] ?? cond;
  }

  energyIcon(type: string): string {
    return ENERGY_ICONS[type] ?? '⚡';
  }

  energyColor(type: string): string {
    return ENERGY_COLORS[type] ?? '#888';
  }
}
