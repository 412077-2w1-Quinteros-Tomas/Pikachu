import { Component, ChangeDetectionStrategy, input, output, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DeckModel } from '../../../../shared/models/deck.model';
import { CreateMatchRequest } from '../../../../shared/models/match.model';

@Component({
  selector: 'app-create-match-dialog',
  templateUrl: './create-match-dialog.html',
  styleUrl: './create-match-dialog.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
})
export class CreateMatchDialogComponent {

  readonly decks = input<DeckModel[]>([]);
  readonly playerName = input('');
  readonly saving = input(false);
  readonly matchCreated = output<CreateMatchRequest>();
  readonly cancelled = output<void>();

  readonly name = signal('');
  readonly selectedDeckId = signal('');
  readonly error = signal('');

  submit(): void {
    if (!this.name().trim()) {
      this.error.set('El nombre de la partida es obligatorio.');
      return;
    }
    if (!this.playerName().trim()) {
      this.error.set('Necesitás un nombre de jugador.');
      return;
    }
    this.error.set('');
    this.matchCreated.emit({
      name: this.name().trim(),
      player1: this.playerName().trim(),
      deckId: this.selectedDeckId() || undefined,
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }
}
