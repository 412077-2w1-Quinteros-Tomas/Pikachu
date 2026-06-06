import { EnergyType, PokemonStage } from './card.model';

export type GamePhase =
  | 'WAITING' | 'SETUP' | 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS' | 'FINISHED';

export type SpecialCondition = 'ASLEEP' | 'BURNED' | 'CONFUSED' | 'PARALYZED' | 'POISONED';

export interface GameAttack {
  name: string;
  cost: string[];
  damage: number;
  effect: string | null;
}

export interface PokemonCard {
  id: string;
  name: string;
  cardType: 'POKEMON';
  hp: number;
  stage: PokemonStage | null;
  types: EnergyType[];
  attacks: GameAttack[];
  weakness: string | null;
  resistance: string | null;
  retreatCost: number;
  imageUrl: string | null;
}

export interface EnergyCard {
  id: string;
  name: string;
  cardType: 'ENERGY';
  energyType: EnergyType;
  imageUrl?: string | null;
}

export interface TrainerCard {
  id: string;
  name: string;
  cardType: 'TRAINER';
  effect: string | null;
  imageUrl?: string | null;
}

export type GameCard = PokemonCard | EnergyCard | TrainerCard;

export interface PokemonInPlay {
  instanceId: string;
  pokemon: PokemonCard;
  currentHp: number;
  attachedEnergies: EnergyCard[];
  specialCondition: SpecialCondition | null;
}

export interface PlayerBoard {
  playerId: string;
  hand: GameCard[];
  deck: GameCard[];
  discardPile: GameCard[];
  activePokemon: PokemonInPlay | null;
  bench: PokemonInPlay[];
  prizeCards: GameCard[];
  hasPlayedEnergyThisTurn: boolean;
  hasAttackedThisTurn: boolean;
  hasRetreatedThisTurn: boolean;
}

export interface GameBoard {
  matchId: string;
  player1Board: PlayerBoard;
  player2Board: PlayerBoard;
  phase: GamePhase;
  currentPlayerId: string;
  turnNumber: number;
  winnerId: string | null;
  actionLog: string[];
}

export interface GameEvent {
  type: string;
  playerId: string;
  data: Record<string, unknown>;
  timestamp: string;
}

export interface MatchSnapshot {
  matchId: string;
  board: GameBoard;
  events: GameEvent[];
  timestamp: string;
}
