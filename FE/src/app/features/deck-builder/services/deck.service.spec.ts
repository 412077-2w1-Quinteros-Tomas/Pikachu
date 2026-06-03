import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DeckService } from './deck.service';
import { DeckModel, CreateDeckRequest } from '../../../shared/models/deck.model';

describe('DeckService', () => {
  let service: DeckService;
  let httpMock: HttpTestingController;

  const mockDeck: DeckModel = {
    id: '1',
    name: 'Test Deck',
    description: 'A test deck',
    cards: [],
    totalCards: 0,
    valid: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DeckService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllDecks should GET /api/decks', () => {
    service.getAllDecks().subscribe(decks => {
      expect(decks).toEqual([mockDeck]);
    });

    const req = httpMock.expectOne('/api/decks');
    expect(req.request.method).toBe('GET');
    req.flush([mockDeck]);
  });

  it('getDeckById should GET /api/decks/:id', () => {
    service.getDeckById('1').subscribe(deck => {
      expect(deck).toEqual(mockDeck);
    });

    const req = httpMock.expectOne('/api/decks/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockDeck);
  });

  it('createDeck should POST /api/decks', () => {
    const request: CreateDeckRequest = { name: 'Test', description: '', cards: [] };

    service.createDeck(request).subscribe(deck => {
      expect(deck).toEqual(mockDeck);
    });

    const req = httpMock.expectOne('/api/decks');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockDeck);
  });

  it('deleteDeck should DELETE /api/decks/:id', () => {
    service.deleteDeck('1').subscribe();

    const req = httpMock.expectOne('/api/decks/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('validateDeck should POST /api/decks/:id/validate', () => {
    const result = { valid: true, errors: [], totalCards: 60 };

    service.validateDeck('1').subscribe(r => {
      expect(r).toEqual(result);
    });

    const req = httpMock.expectOne('/api/decks/1/validate');
    expect(req.request.method).toBe('POST');
    req.flush(result);
  });
});
