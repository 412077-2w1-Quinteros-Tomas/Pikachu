import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { CardModel, CardFilter } from '../../../shared/models/card.model';

const API_BASE = '/api/cards';

@Injectable({ providedIn: 'root' })
export class CardService {

  private readonly http = inject(HttpClient);

  getCards(filter: CardFilter = {}): Observable<CardModel[]> {
    let params = new HttpParams();
    if (filter.name)     params = params.set('name',     filter.name);
    if (filter.cardType) params = params.set('cardType', filter.cardType);
    if (filter.type)     params = params.set('type',     filter.type);
    if (filter.stage)    params = params.set('stage',    filter.stage);
    if (filter.setId)    params = params.set('setId',    filter.setId);
    if (filter.page != null) params = params.set('page', filter.page);
    if (filter.size != null) params = params.set('size', filter.size);

    return this.http.get<CardModel[]>(API_BASE, { params });
  }

  getById(id: string): Observable<CardModel> {
    return this.http.get<CardModel>(`${API_BASE}/${id}`);
  }

  search(name: string): Observable<CardModel[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<CardModel[]>(`${API_BASE}/search`, { params });
  }

  syncSet(setId: string = 'xy1'): Observable<{ synced: number }> {
    return this.http.post<{ synced: number }>(`${API_BASE}/sync`, { setId });
  }
}
