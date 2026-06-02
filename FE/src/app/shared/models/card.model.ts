export type CardType = 'POKEMON' | 'ENERGY' | 'TRAINER';

export type EnergyType =
  | 'FIRE' | 'WATER' | 'GRASS' | 'LIGHTNING' | 'PSYCHIC'
  | 'FIGHTING' | 'DARKNESS' | 'METAL' | 'FAIRY' | 'DRAGON' | 'COLORLESS';

export type PokemonStage = 'BASIC' | 'STAGE1' | 'STAGE2' | 'EX' | 'MEGA';

export interface Attack {
  name: string;
  cost: string[];
  damage: string;
  effect: string | null;
}

export interface Ability {
  name: string;
  effect: string;
}

export interface CardModel {
  id: string;
  externalId: string;
  name: string;
  cardType: CardType;
  hp: number | null;
  stage: PokemonStage | null;
  types: EnergyType[];
  attacks: Attack[];
  abilities: Ability[];
  weakness: string | null;
  resistance: string | null;
  retreatCost: number | null;
  imageUrl: string | null;
  rarity: string | null;
  cardNumber: string;
  setId: string;
}

export interface CardFilter {
  name?: string;
  cardType?: CardType;
  type?: EnergyType;
  stage?: PokemonStage;
  setId?: string;
  page?: number;
  size?: number;
}
