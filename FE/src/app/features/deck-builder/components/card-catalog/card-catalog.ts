import { Component, ChangeDetectionStrategy, output, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModel, CardType, EnergyType, PokemonStage } from '../../../../shared/models/card.model';
import { CardService } from '../../services/card.service';

@Component({
  selector: 'app-card-catalog',
  templateUrl: './card-catalog.html',
  styleUrl: './card-catalog.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
})
export class CardCatalogComponent {

  readonly cardSelected = output<CardModel>();

  private readonly cardService = inject(CardService);

  readonly cards = signal<CardModel[]>([]);
  readonly loading = signal(false);
  readonly nameFilter = signal('');
  readonly typeFilter = signal<CardType | ''>('');
  readonly stageFilter = signal<PokemonStage | ''>('');

  readonly cardTypes: CardType[] = ['POKEMON', 'ENERGY', 'TRAINER'];
  readonly pokemonStages: PokemonStage[] = ['BASIC', 'STAGE1', 'STAGE2', 'EX', 'MEGA'];

  readonly filteredCards = computed(() => {
    const name = this.nameFilter().toLowerCase();
    const type = this.typeFilter();
    const stage = this.stageFilter();
    return this.cards().filter(c =>
      (!name || c.name.toLowerCase().includes(name)) &&
      (!type || c.cardType === type) &&
      (!stage || c.stage === stage)
    );
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.cardService.getCards({ size: 250 }).subscribe({
      next: cards => { this.cards.set(cards); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  select(card: CardModel): void {
    this.cardSelected.emit(card);
  }

  trackById(_: number, card: CardModel): string {
    return card.id;
  }
}
