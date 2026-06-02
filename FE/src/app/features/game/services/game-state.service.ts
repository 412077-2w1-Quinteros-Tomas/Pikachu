import { Injectable, signal, computed } from '@angular/core';
import { GameBoard, MatchSnapshot, PlayerBoard } from '../../../shared/models/game.model';

@Injectable({ providedIn: 'root' })
export class GameStateService {

  private readonly _snapshot = signal<MatchSnapshot | null>(null);
  private readonly _myPlayerId = signal('');
  private readonly _connected = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly snapshot = this._snapshot.asReadonly();
  readonly board = computed(() => this._snapshot()?.board ?? null);
  readonly myPlayerId = this._myPlayerId.asReadonly();
  readonly connected = this._connected.asReadonly();
  readonly error = this._error.asReadonly();

  readonly myBoard = computed<PlayerBoard | null>(() => {
    const board = this.board();
    const id = this._myPlayerId();
    if (!board || !id) return null;
    if (board.player1Board?.playerId === id) return board.player1Board;
    if (board.player2Board?.playerId === id) return board.player2Board;
    return null;
  });

  readonly opponentBoard = computed<PlayerBoard | null>(() => {
    const board = this.board();
    const id = this._myPlayerId();
    if (!board || !id) return null;
    if (board.player1Board?.playerId === id) return board.player2Board;
    return board.player1Board;
  });

  readonly isMyTurn = computed(() =>
    this.board()?.currentPlayerId === this._myPlayerId()
  );

  readonly phase = computed(() => this.board()?.phase ?? null);
  readonly winner = computed(() => this.board()?.winnerId ?? null);
  readonly isFinished = computed(() => this.board()?.phase === 'FINISHED');

  setPlayerId(id: string): void {
    this._myPlayerId.set(id);
  }

  applySnapshot(snapshot: MatchSnapshot): void {
    this._snapshot.set(snapshot);
    this._error.set(null);
  }

  setConnected(connected: boolean): void {
    this._connected.set(connected);
  }

  setError(message: string): void {
    this._error.set(message);
  }

  reset(): void {
    this._snapshot.set(null);
    this._connected.set(false);
    this._error.set(null);
  }
}
