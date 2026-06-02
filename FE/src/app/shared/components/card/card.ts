import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CardModel } from '../../models/card.model';
import { ENERGY_TYPE_MAP } from '../../../core/constants/energy-types';

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './card.html',
  styleUrl: './card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CardComponent {

  card     = input.required<CardModel>();
  selected = input(false);
  compact  = input(false);

  clicked = output<CardModel>();

  readonly energyTypeMap = ENERGY_TYPE_MAP;

  onCardClick(): void {
    this.clicked.emit(this.card());
  }

  getEnergyColor(type: string): string {
    return this.energyTypeMap.get(type as any)?.color ?? '#BDBDBD';
  }
}
