import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-discard-pile',
  standalone: true,
  templateUrl: './discard-pile.html',
  styleUrl: './discard-pile.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DiscardPileComponent {
  readonly count = input<number>(0);
}
