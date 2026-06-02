import { CardModel } from './card.model';

export interface DeckCardItem {
  id: string;
  card: CardModel;
  quantity: number;
}

export interface DeckModel {
  id: string;
  name: string;
  description: string | null;
  cards: DeckCardItem[];
  totalCards: number;
  valid: boolean;
}

export interface CreateDeckCardEntry {
  cardId: string;
  quantity: number;
}

export interface CreateDeckRequest {
  name: string;
  description?: string;
  cards: CreateDeckCardEntry[];
}

export interface DeckValidationResult {
  valid: boolean;
  errors: string[];
  totalCards: number;
}
