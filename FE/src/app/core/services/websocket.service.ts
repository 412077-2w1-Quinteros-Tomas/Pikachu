import { Injectable } from '@angular/core';
import { Observable, Subject, timer } from 'rxjs';
import { GameActionMessage, GameStateUpdateMessage } from '../../shared/models/match.model';

@Injectable({ providedIn: 'root' })
export class WebsocketService {

  private socket: WebSocket | null = null;
  private messages = new Subject<GameStateUpdateMessage>();
  private reconnectDelay = 3000;
  private currentMatchId: string | null = null;
  private currentPlayerId: string | null = null;

  connect(matchId: string, playerId: string): Observable<GameStateUpdateMessage> {
    this.currentMatchId = matchId;
    this.currentPlayerId = playerId;
    this.openSocket(matchId, playerId);
    return this.messages.asObservable();
  }

  send(message: GameActionMessage): void {
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    }
  }

  disconnect(): void {
    this.currentMatchId = null;
    this.currentPlayerId = null;
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }

  get isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN;
  }

  private openSocket(matchId: string, playerId: string): void {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${location.host}/ws/game?matchId=${matchId}&playerId=${encodeURIComponent(playerId)}`;

    this.socket = new WebSocket(url);

    this.socket.onmessage = (event) => {
      try {
        const msg: GameStateUpdateMessage = JSON.parse(event.data);
        this.messages.next(msg);
      } catch {
        // ignore malformed messages
      }
    };

    this.socket.onerror = () => {
      this.messages.next({ type: 'ERROR', matchId, data: { message: 'Connection error' } });
    };

    this.socket.onclose = () => {
      if (this.currentMatchId === matchId) {
        timer(this.reconnectDelay).subscribe(() => {
          if (this.currentMatchId === matchId && this.currentPlayerId) {
            this.openSocket(matchId, this.currentPlayerId);
          }
        });
      }
    };
  }
}
