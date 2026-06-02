import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/pokedex/pages/pokedex-page/pokedex-page').then(m => m.PokedexPage)
  },
  {
    path: 'deck-builder',
    loadComponent: () =>
      import('./features/deck-builder/pages/deck-builder-page/deck-builder-page').then(m => m.DeckBuilderPage)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
