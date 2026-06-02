import { Component, ChangeDetectionStrategy, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatchModel, CreateMatchRequest } from '../../../../shared/models/match.model';
import { DeckModel } from '../../../../shared/models/deck.model';
import { LobbyService } from '../../services/lobby.service';
import { DeckService } from '../../../deck-builder/services/deck.service';
import { MatchListComponent } from '../../components/match-list/match-list';
import { CreateMatchDialogComponent } from '../../components/create-match-dialog/create-match-dialog';

const PLAYER_KEY = 'poke_player_name';
const REFRESH_MS = 5000;

@Component({
  selector: 'app-lobby-page',
  templateUrl: './lobby-page.html',
  styleUrl: './lobby-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, MatchListComponent, CreateMatchDialogComponent],
})
export class LobbyPage implements OnInit, OnDestroy {

  private readonly lobbyService = inject(LobbyService);
  private readonly deckService = inject(DeckService);
  private readonly router = inject(Router);

  readonly matches = signal<MatchModel[]>([]);
  readonly decks = signal<DeckModel[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly showCreate = signal(false);
  readonly showJoinDialog = signal(false);
  readonly joiningMatch = signal<MatchModel | null>(null);
  readonly joinDeckId = signal('');

  readonly playerName = signal(localStorage.getItem(PLAYER_KEY) ?? '');
  readonly editingName = signal(!localStorage.getItem(PLAYER_KEY));
  readonly nameInput = signal(localStorage.getItem(PLAYER_KEY) ?? '');

  private refreshInterval: ReturnType<typeof setInterval> | null = null;
  private sub: Subscription | null = null;

  ngOnInit(): void {
    this.loadMatches();
    this.loadDecks();
    this.refreshInterval = setInterval(() => this.loadMatches(), REFRESH_MS);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
    this.sub?.unsubscribe();
  }

  saveName(): void {
    const name = this.nameInput().trim();
    if (!name) return;
    this.playerName.set(name);
    localStorage.setItem(PLAYER_KEY, name);
    this.editingName.set(false);
  }

  loadMatches(): void {
    this.lobbyService.getWaitingMatches().subscribe({
      next: matches => this.matches.set(matches),
    });
  }

  loadDecks(): void {
    this.deckService.getAllDecks().subscribe({
      next: decks => this.decks.set(decks),
    });
  }

  openCreate(): void {
    if (!this.playerName()) {
      this.editingName.set(true);
      return;
    }
    this.showCreate.set(true);
  }

  onMatchCreated(request: CreateMatchRequest): void {
    this.saving.set(true);
    this.lobbyService.createMatch(request).subscribe({
      next: match => {
        this.saving.set(false);
        this.showCreate.set(false);
        this.loadMatches();
        this.router.navigate(['/game', match.id], {
          queryParams: { player: request.player1 },
        });
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.message ?? 'Error al crear la partida.');
      },
    });
  }

  openJoin(match: MatchModel): void {
    if (!this.playerName()) {
      this.editingName.set(true);
      return;
    }
    this.joiningMatch.set(match);
    this.showJoinDialog.set(true);
  }

  confirmJoin(): void {
    const match = this.joiningMatch();
    if (!match) return;
    this.saving.set(true);
    this.lobbyService.joinMatch(match.id, {
      player2: this.playerName(),
      deckId: this.joinDeckId() || undefined,
    }).subscribe({
      next: updated => {
        this.saving.set(false);
        this.showJoinDialog.set(false);
        this.router.navigate(['/game', updated.id], {
          queryParams: { player: this.playerName() },
        });
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err?.message ?? 'Error al unirse a la partida.');
      },
    });
  }
}
