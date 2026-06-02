import { CardType, PokemonStage } from '../../shared/models/card.model';

export interface CardTypeInfo {
  value: CardType;
  label: string;
}

export interface PokemonStageInfo {
  value: PokemonStage;
  label: string;
}

export const CARD_TYPES: CardTypeInfo[] = [
  { value: 'POKEMON',  label: 'Pokémon'    },
  { value: 'ENERGY',   label: 'Energía'    },
  { value: 'TRAINER',  label: 'Entrenador' },
];

export const POKEMON_STAGES: PokemonStageInfo[] = [
  { value: 'BASIC',  label: 'Básico'    },
  { value: 'STAGE1', label: 'Fase 1'    },
  { value: 'STAGE2', label: 'Fase 2'    },
  { value: 'EX',     label: 'Pokémon-EX' },
  { value: 'MEGA',   label: 'Mega'      },
];
