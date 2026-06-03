import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { PokemonInPlay } from '../../../../shared/models/game.model';
import { ActivePokemonComponent } from '../active-pokemon/active-pokemon';

@Component({
  selector: 'app-bench-area',
  standalone: true,
  templateUrl: './bench-area.html',
  styleUrl: './bench-area.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ActivePokemonComponent]
})
export class BenchAreaComponent {
  readonly bench = input<PokemonInPlay[]>([]);
  readonly clickable = input<boolean>(false);
  readonly benchSelected = output<number>();
}
