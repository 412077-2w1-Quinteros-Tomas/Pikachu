import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { EnergyCard, GameCard, PokemonCard } from '../../../../shared/models/game.model';

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
  selector: 'app-hand-area',
  standalone: true,
  templateUrl: './hand-area.html',
  styleUrl: './hand-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandAreaComponent {
  readonly hand = input<GameCard[]>([]);
  readonly isMyTurn = input<boolean>(false);
  readonly canPlayEnergy = input<boolean>(false);

  readonly cardPlayed = output<string>();
  readonly energyCardSelected = output<string>();

  readonly selectedCardId = signal<string | null>(null);

  onCardClick(card: GameCard): void {
    if (!this.isMyTurn()) return;
    if (card.cardType === 'ENERGY') {
      if (this.canPlayEnergy()) {
        this.energyCardSelected.emit(card.id);
      }
    } else if (card.cardType === 'POKEMON') {
      this.cardPlayed.emit(card.id);
    }
  }

  isPlayable(card: GameCard): boolean {
    if (!this.isMyTurn()) return false;
    if (card.cardType === 'ENERGY') return this.canPlayEnergy();
    if (card.cardType === 'POKEMON') {
      const stage = (card as PokemonCard).stage;
      return stage === 'BASIC' || stage === 'EX';
    }
    return false;
  }

  stageLabel(card: GameCard): string {
    if (card.cardType !== 'POKEMON') return '';
    const stage = (card as PokemonCard).stage;
    const labels: Record<string, string> = { BASIC: '', STAGE1: 'Evol.1', STAGE2: 'Evol.2', EX: 'EX', MEGA: 'MEGA' };
    return labels[stage ?? ''] ?? '';
  }

  energyBg(card: GameCard): string {
    if (card.cardType !== 'ENERGY') return '';
    return ENERGY_COLORS[(card as EnergyCard).energyType] ?? '#888';
  }

  energyIcon(card: GameCard): string {
    if (card.cardType !== 'ENERGY') return '';
    return ENERGY_ICONS[(card as EnergyCard).energyType] ?? '⚡';
  }

  pokemonImg(card: GameCard): string | null {
    if (card.cardType !== 'POKEMON') return null;
    return (card as PokemonCard).imageUrl ?? null;
  }

  pokemonHp(card: GameCard): number | null {
    if (card.cardType !== 'POKEMON') return null;
    return (card as PokemonCard).hp;
  }

  cardTypeLabel(card: GameCard): string {
    return { POKEMON: '⚔️', ENERGY: '⚡', TRAINER: '🃏' }[card.cardType] ?? '';
  }
}
