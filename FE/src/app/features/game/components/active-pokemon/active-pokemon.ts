import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-active-pokemon',
  templateUrl: './active-pokemon.html',
  styleUrl: './active-pokemon.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivePokemonComponent {}
