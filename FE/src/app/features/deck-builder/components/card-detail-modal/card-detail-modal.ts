import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CardModel } from '../../../../shared/models/card.model';

@Component({
  selector: 'app-card-detail-modal',
  templateUrl: './card-detail-modal.html',
  styleUrl: './card-detail-modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(click)': 'onBackdrop($event)' },
})
export class CardDetailModalComponent {

  readonly card = input.required<CardModel>();
  readonly addRequested = output<CardModel>();
  readonly closed = output<void>();

  onBackdrop(event: Event): void {
    if (event.target === event.currentTarget) {
      this.closed.emit();
    }
  }

  add(): void {
    this.addRequested.emit(this.card());
    this.closed.emit();
  }

  close(): void {
    this.closed.emit();
  }
}
