import { TestBed } from '@angular/core/testing';
import { GameStateService } from './game-state.service';
import { MatchSnapshot, GameBoard, PlayerBoard } from '../../../shared/models/game.model';

describe('GameStateService', () => {
  let service: GameStateService;

  const buildBoard = (p1Id: string, p2Id: string): GameBoard => ({
    matchId: 'test-match',
    player1Board: buildPlayerBoard(p1Id),
    player2Board: buildPlayerBoard(p2Id),
    phase: 'MAIN',
    currentPlayerId: p1Id,
    turnNumber: 1,
    winnerId: null,
    actionLog: []
  });

  const buildPlayerBoard = (playerId: string): PlayerBoard => ({
    playerId,
    hand: [],
    deck: [],
    discardPile: [],
    activePokemon: null,
    bench: [],
    prizeCards: [],
    hasPlayedEnergyThisTurn: false,
    hasAttackedThisTurn: false,
    hasRetreatedThisTurn: false
  });

  const buildSnapshot = (p1Id: string, p2Id: string): MatchSnapshot => ({
    matchId: 'test-match',
    board: buildBoard(p1Id, p2Id),
    events: [],
    timestamp: new Date().toISOString()
  });

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GameStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('initial state should have no snapshot', () => {
    expect(service.snapshot()).toBeNull();
    expect(service.board()).toBeNull();
    expect(service.connected()).toBeFalse();
  });

  it('setPlayerId should update myPlayerId', () => {
    service.setPlayerId('player1');
    expect(service.myPlayerId()).toBe('player1');
  });

  it('applySnapshot should update board', () => {
    const snapshot = buildSnapshot('p1', 'p2');
    service.applySnapshot(snapshot);

    expect(service.snapshot()).toBe(snapshot);
    expect(service.board()).toBe(snapshot.board);
    expect(service.error()).toBeNull();
  });

  it('myBoard should return board for current player', () => {
    service.setPlayerId('p1');
    service.applySnapshot(buildSnapshot('p1', 'p2'));

    expect(service.myBoard()?.playerId).toBe('p1');
  });

  it('opponentBoard should return the other player board', () => {
    service.setPlayerId('p1');
    service.applySnapshot(buildSnapshot('p1', 'p2'));

    expect(service.opponentBoard()?.playerId).toBe('p2');
  });

  it('isMyTurn should be true when current player matches', () => {
    service.setPlayerId('p1');
    service.applySnapshot(buildSnapshot('p1', 'p2'));

    expect(service.isMyTurn()).toBeTrue();
  });

  it('isMyTurn should be false when not current player', () => {
    service.setPlayerId('p2');
    service.applySnapshot(buildSnapshot('p1', 'p2'));

    expect(service.isMyTurn()).toBeFalse();
  });

  it('phase should reflect board phase', () => {
    service.applySnapshot(buildSnapshot('p1', 'p2'));
    expect(service.phase()).toBe('MAIN');
  });

  it('isFinished should be false for non-finished phase', () => {
    service.applySnapshot(buildSnapshot('p1', 'p2'));
    expect(service.isFinished()).toBeFalse();
  });

  it('isFinished should be true for FINISHED phase', () => {
    const snap = buildSnapshot('p1', 'p2');
    snap.board.phase = 'FINISHED';
    service.applySnapshot(snap);

    expect(service.isFinished()).toBeTrue();
  });

  it('winner should return winnerId from board', () => {
    const snap = buildSnapshot('p1', 'p2');
    snap.board.winnerId = 'p1';
    service.applySnapshot(snap);

    expect(service.winner()).toBe('p1');
  });

  it('setConnected should update connected signal', () => {
    service.setConnected(true);
    expect(service.connected()).toBeTrue();

    service.setConnected(false);
    expect(service.connected()).toBeFalse();
  });

  it('setError should update error signal', () => {
    service.setError('something went wrong');
    expect(service.error()).toBe('something went wrong');
  });

  it('reset should clear all state', () => {
    service.setPlayerId('p1');
    service.setConnected(true);
    service.applySnapshot(buildSnapshot('p1', 'p2'));

    service.reset();

    expect(service.snapshot()).toBeNull();
    expect(service.connected()).toBeFalse();
    expect(service.error()).toBeNull();
  });
});
