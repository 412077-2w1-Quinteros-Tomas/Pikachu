import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap, startWith, catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import { CardModel, CardType, PokemonStage } from '../../../../shared/models/card.model';
import { CardComponent } from '../../../../shared/components/card/card';
import { CardService } from '../../../deck-builder/services/card.service';
import { CARD_TYPES, POKEMON_STAGES } from '../../../../core/constants/card-types';
import { ENERGY_TYPES } from '../../../../core/constants/energy-types';

@Component({
  selector: 'app-pokedex-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardComponent],
  templateUrl: './pokedex-page.html',
  styleUrl: './pokedex-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PokedexPage {

  private readonly cardService = inject(CardService);

  readonly cardTypes   = CARD_TYPES;
  readonly stages      = POKEMON_STAGES;
  readonly energyTypes = ENERGY_TYPES;

  nameFilter     = signal('');
  cardTypeFilter = signal<CardType | ''>('');
  stageFilter    = signal<PokemonStage | ''>('');

  selectedCard = signal<CardModel | null>(null);
  loading      = signal(false);
  error        = signal<string | null>(null);

  private readonly refreshTick = signal(0);

  private readonly filter = computed(() => ({
    name:     this.nameFilter()     || undefined,
    cardType: (this.cardTypeFilter() || undefined) as CardType | undefined,
    stage:    (this.stageFilter()    || undefined) as PokemonStage | undefined,
    size:     100,
    _tick:    this.refreshTick(),
  }));

  readonly cards = toSignal(
    toObservable(this.filter).pipe(
      debounceTime(300),
      switchMap(filter => {
        this.loading.set(true);
        this.error.set(null);
        return this.cardService.getCards(filter).pipe(
          tap(() => this.loading.set(false)),
          catchError(err => {
            this.error.set(err.message ?? 'Error al cargar las cartas');
            this.loading.set(false);
            return of([] as CardModel[]);
          })
        );
      }),
      startWith([] as CardModel[])
    ),
    { initialValue: [] as CardModel[] }
  );

  onCardClicked(card: CardModel): void {
    this.selectedCard.set(card);
  }

  closeDetail(): void {
    this.selectedCard.set(null);
  }

  syncCards(): void {
    this.loading.set(true);
    this.error.set(null);
    this.cardService.syncSet('xy1').subscribe({
      next: () => {
        this.loading.set(false);
        this.refreshTick.update(n => n + 1);
      },
      error: err => {
        this.error.set(err.message ?? 'Error al sincronizar');
        this.loading.set(false);
      }
    });
  }
}
