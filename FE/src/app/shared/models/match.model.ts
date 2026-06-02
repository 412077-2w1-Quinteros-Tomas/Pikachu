export type MatchStatus = 'WAITING' | 'IN_PROGRESS' | 'FINISHED';

export interface MatchModel {
  id: string;
  name: string;
  player1: string;
  player2: string | null;
  deck1Id: string | null;
  deck2Id: string | null;
  status: MatchStatus;
  createdAt: string;
}

export interface CreateMatchRequest {
  name: string;
  player1: string;
  deckId?: string;
}

export interface JoinMatchRequest {
  player2: string;
  deckId?: string;
}

export interface GameActionMessage {
  matchId: string;
  playerId: string;
  actionType: string;
  payload?: Record<string, unknown>;
}

export interface GameStateUpdateMessage {
  type: string;
  matchId: string;
  data: unknown;
}
