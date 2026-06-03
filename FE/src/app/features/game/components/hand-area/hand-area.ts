import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { EnergyCard, GameCard, PokemonCard, PokemonInPlay } from '../../../../shared/models/game.model';
import { EnergyAttachmentComponent } from '../energy-attachment/energy-attachment';

@Component({
  selector: 'app-hand-area',
  standalone: true,
  templateUrl: './hand-area.html',
  styleUrl: './hand-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EnergyAttachmentComponent]
})
export class HandAreaComponent {
  readonly hand = input<GameCard[]>([]);
  readonly isMyTurn = input<boolean>(false);
  readonly canPlayEnergy = input<boolean>(false);
  readonly availableTargets = input<PokemonInPlay[]>([]);

  readonly cardPlayed = output<string>();
  readonly energyAttached = output<{ energyCardId: string; targetInstanceId: string }>();

  readonly showEnergyModal = signal(false);
  readonly selectedCardId = signal<string | null>(null);

  get energyCards(): EnergyCard[] {
    return this.hand().filter((c): c is EnergyCard => c.cardType === 'ENERGY') as EnergyCard[];
  }

  cardLabel(card: GameCard): string {
    if (card.cardType === 'POKEMON') return `[P] ${card.name}`;
    if (card.cardType === 'ENERGY') return `[E] ${(card as EnergyCard).energyType}`;
    return `[T] ${card.name}`;
  }

  cardClass(card: GameCard): string {
    return `hand-card hand-card--${card.cardType.toLowerCase()}`;
  }

  onCardClick(card: GameCard): void {
    if (!this.isMyTurn()) return;
    if (card.cardType === 'ENERGY' && this.canPlayEnergy()) {
      this.showEnergyModal.set(true);
    } else if (card.cardType === 'POKEMON') {
      this.cardPlayed.emit(card.id);
    }
  }

  onEnergyAttached(event: { energyCardId: string; targetInstanceId: string }): void {
    this.showEnergyModal.set(false);
    this.energyAttached.emit(event);
  }
}
