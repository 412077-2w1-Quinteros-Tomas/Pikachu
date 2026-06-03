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

  get typeGradient(): string {
    const types = this.pokemon()?.pokemon?.types;
    if (!types || types.length === 0) return 'linear-gradient(160deg,#1a1a2e,#16213e)';
    const g: Record<string, string> = {
      FIRE:      'linear-gradient(160deg,#3d0000,#7a1500)',
      WATER:     'linear-gradient(160deg,#001840,#003d80)',
      GRASS:     'linear-gradient(160deg,#001800,#074000)',
      LIGHTNING: 'linear-gradient(160deg,#2a2000,#4a3a00)',
      PSYCHIC:   'linear-gradient(160deg,#200030,#40006a)',
      FIGHTING:  'linear-gradient(160deg,#2a1000,#5a2500)',
      DARKNESS:  'linear-gradient(160deg,#080810,#121222)',
      METAL:     'linear-gradient(160deg,#1a1a22,#2e3242)',
      FAIRY:     'linear-gradient(160deg,#2d0020,#600040)',
      DRAGON:    'linear-gradient(160deg,#0a0020,#200055)',
      COLORLESS: 'linear-gradient(160deg,#1a1a1a,#2a2a2a)',
    };
    return g[types[0]] ?? 'linear-gradient(160deg,#1a1a2e,#16213e)';
  }

  get typeAccent(): string {
    const types = this.pokemon()?.pokemon?.types;
    if (!types || types.length === 0) return '#333';
    const a: Record<string, string> = {
      FIRE:'#ff5500', WATER:'#0088ff', GRASS:'#33cc00',
      LIGHTNING:'#ffdd00', PSYCHIC:'#cc44ff', FIGHTING:'#cc6600',
      DARKNESS:'#5566bb', METAL:'#99aacc', FAIRY:'#ff66aa',
      DRAGON:'#7755dd', COLORLESS:'#666',
    };
    return a[types[0]] ?? '#333';
  }

  get hpLabel(): string {
    const p = this.pokemon();
    if (!p) return '';
    return `${p.currentHp}/${p.pokemon.hp}`;
  }
}
