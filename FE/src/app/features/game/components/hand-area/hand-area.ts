import { Component, ChangeDetectionStrategy, input, output, signal, OnDestroy } from '@angular/core';
import { EnergyCard, GameCard, PokemonCard, TrainerCard } from '../../../../shared/models/game.model';

const ENERGY_COLORS: Record<string, string> = {
  FIRE: '#ff5533', WATER: '#3388ff', GRASS: '#44bb44',
  LIGHTNING: '#ffcc00', PSYCHIC: '#cc44cc', FIGHTING: '#cc6600',
  DARKNESS: '#334466', METAL: '#999999', FAIRY: '#ff88bb',
  DRAGON: '#7755cc', COLORLESS: '#aaaaaa'
};

const ENERGY_GLOWS: Record<string, string> = {
  FIRE: 'rgba(255,85,51,0.5)', WATER: 'rgba(51,136,255,0.5)', GRASS: 'rgba(68,187,68,0.5)',
  LIGHTNING: 'rgba(255,204,0,0.5)', PSYCHIC: 'rgba(204,68,204,0.5)', FIGHTING: 'rgba(204,102,0,0.5)',
  DARKNESS: 'rgba(51,68,102,0.5)', METAL: 'rgba(153,153,153,0.5)', FAIRY: 'rgba(255,136,187,0.5)',
  DRAGON: 'rgba(119,85,204,0.5)', COLORLESS: 'rgba(170,170,170,0.5)'
};

const ENERGY_ICONS: Record<string, string> = {
  FIRE: '🔥', WATER: '💧', GRASS: '🌿', LIGHTNING: '⚡',
  PSYCHIC: '🔮', FIGHTING: '👊', DARKNESS: '🌑', METAL: '⚙️',
  FAIRY: '✨', DRAGON: '🐉', COLORLESS: '⭐'
};

const ENERGY_NAMES_ES: Record<string, string> = {
  FIRE: 'Fuego', WATER: 'Agua', GRASS: 'Planta', LIGHTNING: 'Rayo',
  PSYCHIC: 'Psíquico', FIGHTING: 'Lucha', DARKNESS: 'Oscuridad',
  METAL: 'Metal', FAIRY: 'Hada', DRAGON: 'Dragón', COLORLESS: 'Incoloro'
};

const SUPPORTER_NAMES = new Set([
  'professor sycamore', 'tierno', 'shauna', 'lysandre',
  'team flare grunt', 'pokemon center lady', 'pokémon center lady',
  'colress', 'team plasma grunt', 'n', 'skyla', 'bianca',
  'cheren', 'caitlin', 'hugh', 'juniper', 'professor juniper',
  'flannery', 'korrina', 'wally', 'ace trainer', 'pokemon fan club',
  'pokémon fan club', 'birch', 'elm', 'rowan', 'kukui', 'lillie'
]);

const STADIUM_NAMES = new Set([
  'mountain ring', 'dimensional valley', 'silent lab',
  'training center', 'pokemon center', 'pokémon center',
  'sky field', 'fairy garden', 'rough seas', 'scorched earth',
  'shrine of memories'
]);

const TOOL_KEYWORDS = ['band', 'charm', 'share', 'visor', 'scope', 'brace', 'helmet', 'spirit link'];

export type TrainerSubtype = 'ITEM' | 'SUPPORTER' | 'STADIUM' | 'TOOL';

@Component({
  selector: 'app-hand-area',
  standalone: true,
  templateUrl: './hand-area.html',
  styleUrl: './hand-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandAreaComponent implements OnDestroy {
  readonly hand = input<GameCard[]>([]);
  readonly isMyTurn = input<boolean>(false);
  readonly canPlayEnergy = input<boolean>(false);

  readonly cardPlayed = output<string>();
  readonly energyCardSelected = output<string>();

  readonly zoomedCard = signal<GameCard | null>(null);
  private readonly _failedImages = signal<Set<string>>(new Set());

  private holdTimer: ReturnType<typeof setTimeout> | null = null;
  private didZoom = false;

  ngOnDestroy(): void {
    this.clearHoldTimer();
  }

  // ── Interaction handlers ──────────────────────────────────────────────────

  onPointerDown(card: GameCard, event: Event): void {
    this.didZoom = false;
    this.clearHoldTimer();
    this.holdTimer = setTimeout(() => {
      this.didZoom = true;
      this.zoomedCard.set(card);
    }, 400);
  }

  onPointerUp(): void {
    this.clearHoldTimer();
  }

  onPointerLeave(): void {
    this.clearHoldTimer();
  }

  onCardClick(card: GameCard): void {
    if (this.didZoom) return;
    if (!this.isMyTurn()) return;

    if (card.cardType === 'ENERGY') {
      if (this.canPlayEnergy()) this.energyCardSelected.emit(card.id);
    } else if (card.cardType === 'POKEMON') {
      this.cardPlayed.emit(card.id);
    } else if (card.cardType === 'TRAINER') {
      this.cardPlayed.emit(card.id);
    }
  }

  closeZoom(): void {
    this.zoomedCard.set(null);
  }

  imgFailed(cardId: string): boolean {
    return this._failedImages().has(cardId);
  }

  onImgError(cardId: string): void {
    this._failedImages.update(s => new Set([...s, cardId]));
  }

  private clearHoldTimer(): void {
    if (this.holdTimer !== null) {
      clearTimeout(this.holdTimer);
      this.holdTimer = null;
    }
  }

  // ── Playability ───────────────────────────────────────────────────────────

  isPlayable(card: GameCard): boolean {
    if (!this.isMyTurn()) return false;
    if (card.cardType === 'ENERGY') return this.canPlayEnergy();
    if (card.cardType === 'TRAINER') return true;
    if (card.cardType === 'POKEMON') {
      const stage = (card as PokemonCard).stage;
      return stage === 'BASIC' || stage === 'EX';
    }
    return false;
  }

  // ── Pokémon helpers ───────────────────────────────────────────────────────

  stageLabel(card: GameCard): string {
    if (card.cardType !== 'POKEMON') return '';
    const stage = (card as PokemonCard).stage;
    const labels: Record<string, string> = {
      BASIC: 'Básico', STAGE1: 'Fase 1', STAGE2: 'Fase 2', EX: 'EX', MEGA: 'MEGA'
    };
    return labels[stage ?? ''] ?? '';
  }

  pokemonImg(card: GameCard): string | null {
    if (card.cardType !== 'POKEMON') return null;
    return (card as PokemonCard).imageUrl ?? null;
  }

  pokemonHp(card: GameCard): number | null {
    if (card.cardType !== 'POKEMON') return null;
    return (card as PokemonCard).hp;
  }

  pokemonTypes(card: GameCard): string {
    if (card.cardType !== 'POKEMON') return '';
    const types = (card as PokemonCard).types ?? [];
    return types.map(t => ENERGY_ICONS[t] ?? '').join('');
  }

  // ── Energy helpers ────────────────────────────────────────────────────────

  energyType(card: GameCard): string {
    if (card.cardType !== 'ENERGY') return 'COLORLESS';
    return (card as EnergyCard).energyType ?? 'COLORLESS';
  }

  energyBg(card: GameCard): string {
    return ENERGY_COLORS[this.energyType(card)] ?? '#888';
  }

  energyGlow(card: GameCard): string {
    return ENERGY_GLOWS[this.energyType(card)] ?? 'rgba(136,136,136,0.4)';
  }

  energyIcon(card: GameCard): string {
    return ENERGY_ICONS[this.energyType(card)] ?? '⚡';
  }

  energyNameEs(card: GameCard): string {
    return ENERGY_NAMES_ES[this.energyType(card)] ?? 'Energía';
  }

  energyImg(card: GameCard): string | null {
    if (card.cardType !== 'ENERGY') return null;
    return (card as EnergyCard).imageUrl ?? null;
  }

  // ── Trainer helpers ───────────────────────────────────────────────────────

  trainerSubtype(card: GameCard): TrainerSubtype {
    if (card.cardType !== 'TRAINER') return 'ITEM';
    const name = card.name.toLowerCase();
    if (SUPPORTER_NAMES.has(name)) return 'SUPPORTER';
    if (STADIUM_NAMES.has(name)) return 'STADIUM';
    if (TOOL_KEYWORDS.some(kw => name.includes(kw))) return 'TOOL';
    return 'ITEM';
  }

  trainerBg(card: GameCard): string {
    const sub = this.trainerSubtype(card);
    const gradients: Record<TrainerSubtype, string> = {
      SUPPORTER: 'linear-gradient(150deg, #1a0a40 0%, #3d1a80 50%, #1a0a40 100%)',
      ITEM:      'linear-gradient(150deg, #0a2818 0%, #145228 50%, #0a2818 100%)',
      STADIUM:   'linear-gradient(150deg, #2e1200 0%, #6b3000 50%, #2e1200 100%)',
      TOOL:      'linear-gradient(150deg, #1a1600 0%, #3d3500 50%, #1a1600 100%)',
    };
    return gradients[sub];
  }

  trainerBorderColor(card: GameCard): string {
    const sub = this.trainerSubtype(card);
    const colors: Record<TrainerSubtype, string> = {
      SUPPORTER: '#7c3aed', ITEM: '#16a34a', STADIUM: '#ea580c', TOOL: '#ca8a04'
    };
    return colors[sub];
  }

  trainerIcon(card: GameCard): string {
    const sub = this.trainerSubtype(card);
    const icons: Record<TrainerSubtype, string> = {
      SUPPORTER: '🧑‍🔬', ITEM: '📦', STADIUM: '🏟️', TOOL: '🔧'
    };
    return icons[sub];
  }

  trainerSubtypeLabel(card: GameCard): string {
    const sub = this.trainerSubtype(card);
    const labels: Record<TrainerSubtype, string> = {
      SUPPORTER: 'Partidario', ITEM: 'Objeto', STADIUM: 'Estadio', TOOL: 'Herramienta'
    };
    return labels[sub];
  }

  trainerImg(card: GameCard): string | null {
    if (card.cardType !== 'TRAINER') return null;
    return (card as TrainerCard).imageUrl ?? null;
  }

  // ── Universal zoom image ──────────────────────────────────────────────────

  cardImg(card: GameCard): string | null {
    if (card.cardType === 'POKEMON') return (card as PokemonCard).imageUrl ?? null;
    if (card.cardType === 'TRAINER') return (card as TrainerCard).imageUrl ?? null;
    if (card.cardType === 'ENERGY') return (card as EnergyCard).imageUrl ?? null;
    return null;
  }
}
