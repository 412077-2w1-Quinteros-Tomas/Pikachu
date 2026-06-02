import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DeckModel, CreateDeckRequest, DeckValidationResult } from '../../../shared/models/deck.model';

const API_BASE = '/api/decks';

@Injectable({ providedIn: 'root' })
export class DeckService {

  private readonly http = inject(HttpClient);

  getAllDecks(): Observable<DeckModel[]> {
    return this.http.get<DeckModel[]>(API_BASE);
  }

  getDeckById(id: string): Observable<DeckModel> {
    return this.http.get<DeckModel>(`${API_BASE}/${id}`);
  }

  createDeck(request: CreateDeckRequest): Observable<DeckModel> {
    return this.http.post<DeckModel>(API_BASE, request);
  }

  updateDeck(id: string, request: CreateDeckRequest): Observable<DeckModel> {
    return this.http.put<DeckModel>(`${API_BASE}/${id}`, request);
  }

  deleteDeck(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/${id}`);
  }

  validateDeck(id: string): Observable<DeckValidationResult> {
    return this.http.post<DeckValidationResult>(`${API_BASE}/${id}/validate`, {});
  }
}
