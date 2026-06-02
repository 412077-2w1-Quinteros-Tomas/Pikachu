import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MatchModel, CreateMatchRequest, JoinMatchRequest } from '../../../shared/models/match.model';

const API_BASE = '/api/matches';

@Injectable({ providedIn: 'root' })
export class LobbyService {

  private readonly http = inject(HttpClient);

  getAllMatches(): Observable<MatchModel[]> {
    return this.http.get<MatchModel[]>(API_BASE);
  }

  getWaitingMatches(): Observable<MatchModel[]> {
    const params = new HttpParams().set('status', 'WAITING');
    return this.http.get<MatchModel[]>(API_BASE, { params });
  }

  getMatchById(id: string): Observable<MatchModel> {
    return this.http.get<MatchModel>(`${API_BASE}/${id}`);
  }

  createMatch(request: CreateMatchRequest): Observable<MatchModel> {
    return this.http.post<MatchModel>(API_BASE, request);
  }

  joinMatch(id: string, request: JoinMatchRequest): Observable<MatchModel> {
    return this.http.post<MatchModel>(`${API_BASE}/${id}/join`, request);
  }
}
