/**
 * GameStateMachineService
 *
 * Cliente de Máquina de Estados para Pokémon TCG XY1.
 * Valida acciones localmente antes de enviarlas al servidor,
 * expone la fase actual como signal reactivo y gestiona
 * las transiciones de estado del turno.
 *
 * Fases:
 *   WAITING → SETUP → DRAW → MAIN → BETWEEN_TURNS → (loop) / FINISHED
 */
import { Injectable, signal, computed } from '@angular/core';
import {
  GameBoard, GameCard, GamePhase, PlayerBoard,
  PokemonCard, EnergyCard, TrainerCard, PokemonInPlay, SpecialCondition
} from '../../../shared/models/game.model';

// ── Tipos internos ─────────────────────────────────────────────────────────

export type TurnAction =
  | { type: 'DRAW' }
  | { type: 'PLAY_POKEMON';   cardId: string }
  | { type: 'EVOLVE';         cardId: string; targetInstanceId: string }
  | { type: 'ATTACH_ENERGY';  energyCardId: string; targetInstanceId: string }
  | { type: 'PLAY_TRAINER';   cardId: string }
  | { type: 'ATTACK';         attackIndex: number }
  | { type: 'RETREAT';        benchIndex: number }
  | { type: 'END_TURN' };

export interface ValidationResult {
  valid: boolean;
  reason?: string;
}

export interface TurnConstraints {
  hasDrawn: boolean;
  hasAttachedEnergy: boolean;
  hasAttacked: boolean;
  hasRetreated: boolean;
  hasPlayedSupporter: boolean;
  hasPlayedStadium: boolean;
  playedPokemonIds: Set<string>;   // instanceIds placed this turn (can't evolve same turn)
}

export interface SpecialConditionResolution {
  condition: SpecialCondition;
  result: 'DAMAGE' | 'WAKE_UP' | 'CURE' | 'STAY' | 'NO_ATTACK';
  damage?: number;
  message: string;
}

// ── XY1 Card Validation ────────────────────────────────────────────────────

const XY1_SET_ID = 'xy1';

const WEAKNESS_MULTIPLIER = 2;      // XY era: ×2 weakness
const RESISTANCE_REDUCTION = 20;    // XY era: −20 resistance

const SUPPORTER_NAMES = new Set([
  'professor sycamore', 'tierno', 'shauna', 'lysandre',
  'team flare grunt', 'pokemon center lady', 'pokémon center lady',
  'colress', 'team plasma grunt', 'wally', 'ace trainer',
  'pokemon fan club', 'pokémon fan club', 'flannery', 'korrina',
]);

const STADIUM_NAMES = new Set([
  'mountain ring', 'dimensional valley', 'silent lab',
  'training center', 'sky field', 'fairy garden', 'rough seas',
  'scorched earth', 'shrine of memories',
]);

// ── Service ────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class GameStateMachineService {

  // ── Reactive state ────────────────────────────────────────────────────────

  private readonly _board    = signal<GameBoard | null>(null);
  private readonly _myId     = signal<string>('');
  private readonly _constraints = signal<TurnConstraints>(this.freshConstraints());

  readonly board         = this._board.asReadonly();
  readonly myId          = this._myId.asReadonly();
  readonly constraints   = this._constraints.asReadonly();

  readonly phase = computed<GamePhase>(() => this._board()?.phase ?? 'WAITING');
  readonly isMyTurn = computed(() =>
    this._board()?.currentPlayerId === this._myId()
  );

  readonly myBoard = computed<PlayerBoard | null>(() => {
    const b = this._board();
    if (!b || !this._myId()) return null;
    return b.player1Board.playerId === this._myId()
      ? b.player1Board
      : b.player2Board;
  });

  readonly opponentBoard = computed<PlayerBoard | null>(() => {
    const b = this._board();
    if (!b || !this._myId()) return null;
    return b.player1Board.playerId === this._myId()
      ? b.player2Board
      : b.player1Board;
  });

  readonly isFirstTurn = computed(() => this._board()?.turnNumber === 1);

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  setPlayerId(id: string): void {
    this._myId.set(id);
  }

  applyServerBoard(board: GameBoard): void {
    this._board.set(board);
    // Sync constraints from server flags
    const pb = this.myBoard();
    if (pb) {
      this._constraints.update(c => ({
        ...c,
        hasAttachedEnergy: pb.hasPlayedEnergyThisTurn,
        hasAttacked:       pb.hasAttackedThisTurn,
        hasRetreated:      pb.hasRetreatedThisTurn,
      }));
    }
  }

  onTurnStart(): void {
    this._constraints.set(this.freshConstraints());
  }

  // ── Validation layer ─────────────────────────────────────────────────────

  /**
   * Valida si una acción es legal según las reglas oficiales de XY1.
   * Llame esto ANTES de enviar la acción al servidor para feedback inmediato.
   */
  validate(action: TurnAction): ValidationResult {
    const board = this._board();
    if (!board) return { valid: false, reason: 'Sin tablero activo' };
    if (!this.isMyTurn()) return { valid: false, reason: 'No es tu turno' };

    const pb    = this.myBoard()!;
    const opp   = this.opponentBoard()!;
    const phase = board.phase;
    const c     = this._constraints();

    switch (action.type) {

      // ── Robar carta ────────────────────────────────────────────────────
      case 'DRAW':
        if (phase !== 'DRAW') return fail('Solo puedes robar en la fase de robo');
        if (c.hasDrawn)       return fail('Ya robaste esta fase');
        if (pb.deck.length === 0) return fail('Tu mazo está vacío — pierdes');
        return ok();

      // ── Jugar Pokémon Básico ────────────────────────────────────────────
      case 'PLAY_POKEMON': {
        if (phase !== 'MAIN') return fail('Solo puedes jugar cartas en la fase principal');
        const card = findInHand(pb, action.cardId);
        if (!card)            return fail('Carta no encontrada en la mano');
        if (card.cardType !== 'POKEMON') return fail('No es un Pokémon');
        const poke = card as PokemonCard;
        if (poke.stage !== 'BASIC' && poke.stage !== 'EX')
          return fail('Solo puedes jugar Pokémon Básicos o EX directamente desde la mano');
        if (!pb.activePokemon)  return ok(); // Zona activa libre
        if (pb.bench.length >= 5) return fail('La Banca está llena (máximo 5 Pokémon)');
        return ok();
      }

      // ── Evolucionar ───────────────────────────────────────────────────
      case 'EVOLVE': {
        if (phase !== 'MAIN') return fail('Solo puedes evolucionar en la fase principal');
        if (this.isFirstTurn()) return fail('No puedes evolucionar en el primer turno del juego');
        const card = findInHand(pb, action.cardId);
        if (!card || card.cardType !== 'POKEMON') return fail('Carta de evolución no válida');
        const evo = card as PokemonCard;
        const target = findInPlay(pb, action.targetInstanceId);
        if (!target) return fail('Pokémon objetivo no encontrado en juego');
        if (c.playedPokemonIds.has(action.targetInstanceId))
          return fail('No puedes evolucionar un Pokémon el mismo turno en que entró en juego');
        if (!canEvolveInto(evo, target.pokemon))
          return fail(`${evo.name} no puede evolucionar de ${target.pokemon.name}`);
        return ok();
      }

      // ── Adjuntar energía ─────────────────────────────────────────────
      case 'ATTACH_ENERGY': {
        if (phase !== 'MAIN') return fail('Solo puedes adjuntar energía en la fase principal');
        if (c.hasAttachedEnergy) return fail('Ya adjuntaste una energía este turno');
        const energy = findInHand(pb, action.energyCardId);
        if (!energy || energy.cardType !== 'ENERGY')
          return fail('Carta de energía no encontrada en la mano');
        const target = findInPlay(pb, action.targetInstanceId);
        if (!target) return fail('Pokémon objetivo no encontrado en juego');
        return ok();
      }

      // ── Jugar carta de Entrenador ─────────────────────────────────────
      case 'PLAY_TRAINER': {
        if (phase !== 'MAIN') return fail('Solo puedes jugar entrenadores en la fase principal');
        const card = findInHand(pb, action.cardId);
        if (!card || card.cardType !== 'TRAINER') return fail('Carta de entrenador no encontrada');
        const sub = trainerSubtype(card.name);
        if (sub === 'SUPPORTER' && c.hasPlayedSupporter)
          return fail('Solo puedes jugar 1 carta de Partidario por turno');
        if (sub === 'STADIUM' && c.hasPlayedStadium)
          return fail('Solo puedes jugar 1 carta de Estadio por turno');
        return ok();
      }

      // ── Atacar ────────────────────────────────────────────────────────
      case 'ATTACK': {
        if (phase !== 'MAIN') return fail('Solo puedes atacar en la fase principal');
        if (c.hasAttacked)    return fail('Ya atacaste este turno');
        if (this.isFirstTurn()) return fail('El jugador que empieza no puede atacar en su primer turno');
        if (!pb.activePokemon) return fail('No tienes Pokémon activo');
        const active = pb.activePokemon;
        if (active.specialCondition === 'ASLEEP')
          return fail('Tu Pokémon está Dormido y no puede atacar');
        if (active.specialCondition === 'PARALYZED')
          return fail('Tu Pokémon está Paralizado y no puede atacar');
        const attacks = active.pokemon.attacks ?? [];
        if (action.attackIndex >= attacks.length)
          return fail('Índice de ataque inválido');
        const atk = attacks[action.attackIndex];
        if (!hasEnoughEnergy(active, atk.cost))
          return fail(`Energía insuficiente para usar ${atk.name}`);
        return ok();
      }

      // ── Retirar ───────────────────────────────────────────────────────
      case 'RETREAT': {
        if (phase !== 'MAIN') return fail('Solo puedes retirar en la fase principal');
        if (c.hasRetreated)   return fail('Ya retiraste un Pokémon este turno');
        if (!pb.activePokemon) return fail('No tienes Pokémon activo');
        if (pb.bench.length === 0) return fail('No hay Pokémon en la Banca');
        if (action.benchIndex >= pb.bench.length)
          return fail('Índice de Banca inválido');
        const active = pb.activePokemon;
        if (active.specialCondition === 'ASLEEP' || active.specialCondition === 'PARALYZED')
          return fail('Tu Pokémon no puede retirarse en este estado');
        const retreatCost = active.pokemon.retreatCost ?? 0;
        if (active.attachedEnergies.length < retreatCost)
          return fail(`Necesitas ${retreatCost} energías para retirarte (tienes ${active.attachedEnergies.length})`);
        return ok();
      }

      // ── Finalizar turno ───────────────────────────────────────────────
      case 'END_TURN':
        if (phase !== 'MAIN' && phase !== 'BETWEEN_TURNS')
          return fail('No puedes finalizar el turno en esta fase');
        return ok();

      default:
        return fail('Acción desconocida');
    }
  }

  // ── Registrar acción ejecutada ────────────────────────────────────────────

  /** Actualiza las restricciones locales cuando el servidor confirma una acción. */
  recordAction(action: TurnAction): void {
    this._constraints.update(c => {
      const next = { ...c, playedPokemonIds: new Set(c.playedPokemonIds) };
      switch (action.type) {
        case 'DRAW':           next.hasDrawn = true; break;
        case 'ATTACH_ENERGY':  next.hasAttachedEnergy = true; break;
        case 'ATTACK':         next.hasAttacked = true; break;
        case 'RETREAT':        next.hasRetreated = true; break;
        case 'PLAY_TRAINER': {
          const card = findInHand(this.myBoard()!, action.cardId);
          if (card) {
            const sub = trainerSubtype(card.name);
            if (sub === 'SUPPORTER') next.hasPlayedSupporter = true;
            if (sub === 'STADIUM')   next.hasPlayedStadium   = true;
          }
          break;
        }
        case 'PLAY_POKEMON':
          // Instance ID will be assigned by server; tracked on next snapshot
          break;
      }
      return next;
    });
  }

  // ── Cálculo de daño ─────────────────────────────────────────────────────

  /**
   * Calcula el daño final aplicando Debilidad y Resistencia según reglas XY1.
   * Útil para mostrar el daño potencial en la UI antes de confirmar el ataque.
   */
  calculateDamage(baseDamage: number, attacker: PokemonInPlay, defender: PokemonInPlay): number {
    let dmg = baseDamage;

    // Confusión: daño a sí mismo al intentar atacar
    if (attacker.specialCondition === 'CONFUSED') {
      dmg = 30; // daño fijo por confusión (se resuelve en backend)
    }

    // Debilidad (×2 en era XY)
    const attackerTypes = attacker.pokemon.types ?? [];
    const weakness = defender.pokemon.weakness;
    if (weakness && attackerTypes.some(t => t === weakness)) {
      dmg *= WEAKNESS_MULTIPLIER;
    }

    // Resistencia (−20 en era XY)
    const resistance = defender.pokemon.resistance;
    if (resistance && attackerTypes.some(t => t === resistance)) {
      dmg = Math.max(0, dmg - RESISTANCE_REDUCTION);
    }

    return dmg;
  }

  // ── Resolución de Condiciones Especiales ─────────────────────────────────

  /**
   * Simula la resolución de condiciones especiales al final del turno.
   * Orden oficial: Envenenado → Quemado → Dormido → Paralizado.
   */
  resolveSpecialConditions(pokemon: PokemonInPlay): SpecialConditionResolution[] {
    const results: SpecialConditionResolution[] = [];
    const cond = pokemon.specialCondition;
    if (!cond) return results;

    // Envenenado: 10 daño entre turnos
    if (cond === 'POISONED') {
      results.push({
        condition: 'POISONED',
        result: 'DAMAGE',
        damage: 10,
        message: `${pokemon.pokemon.name} recibe 10 de daño por Envenenamiento`
      });
    }

    // Quemado: 20 daño + tirar moneda para curar
    if (cond === 'BURNED') {
      const coinFlip = Math.random() < 0.5;
      results.push({
        condition: 'BURNED',
        result: coinFlip ? 'CURE' : 'DAMAGE',
        damage: coinFlip ? 0 : 20,
        message: coinFlip
          ? `${pokemon.pokemon.name} se curó de la Quemadura`
          : `${pokemon.pokemon.name} recibe 20 de daño por Quemadura`
      });
    }

    // Dormido: tirar moneda para despertar
    if (cond === 'ASLEEP') {
      const wakeUp = Math.random() < 0.5;
      results.push({
        condition: 'ASLEEP',
        result: wakeUp ? 'WAKE_UP' : 'STAY',
        message: wakeUp
          ? `${pokemon.pokemon.name} se despertó`
          : `${pokemon.pokemon.name} sigue Dormido`
      });
    }

    // Paralizado: se cura automáticamente al final del turno (siempre)
    if (cond === 'PARALYZED') {
      results.push({
        condition: 'PARALYZED',
        result: 'CURE',
        message: `${pokemon.pokemon.name} ya no está Paralizado`
      });
    }

    return results;
  }

  // ── Consultas de estado ───────────────────────────────────────────────────

  /** Cartas jugables de la mano en la fase actual. */
  getPlayableCards(): GameCard[] {
    const pb = this.myBoard();
    if (!pb || !this.isMyTurn()) return [];
    const phase = this.phase();
    if (phase !== 'MAIN') return [];

    return pb.hand.filter(card => this.validate(this.actionForCard(card)).valid);
  }

  /** Pokémon de la mano que pueden evolucionar un Pokémon en juego. */
  getEvolutionTargets(evolutionCardId: string): PokemonInPlay[] {
    const pb = this.myBoard();
    if (!pb) return [];
    const card = findInHand(pb, evolutionCardId);
    if (!card || card.cardType !== 'POKEMON') return [];
    const evo = card as PokemonCard;
    const candidates: PokemonInPlay[] = [];
    if (pb.activePokemon && canEvolveInto(evo, pb.activePokemon.pokemon))
      candidates.push(pb.activePokemon);
    for (const bench of pb.bench) {
      if (canEvolveInto(evo, bench.pokemon)) candidates.push(bench);
    }
    return candidates;
  }

  /** Ataques disponibles con su viabilidad y daño calculado. */
  getAvailableAttacks(): Array<{ index: number; name: string; canUse: boolean; reason?: string }> {
    const pb  = this.myBoard();
    const opp = this.opponentBoard();
    if (!pb?.activePokemon || !opp?.activePokemon) return [];

    return (pb.activePokemon.pokemon.attacks ?? []).map((atk, i) => {
      const result = this.validate({ type: 'ATTACK', attackIndex: i });
      return {
        index: i,
        name:   atk.name,
        canUse: result.valid,
        reason: result.reason,
      };
    });
  }

  // ── Helpers privados ──────────────────────────────────────────────────────

  private freshConstraints(): TurnConstraints {
    return {
      hasDrawn:          false,
      hasAttachedEnergy: false,
      hasAttacked:       false,
      hasRetreated:      false,
      hasPlayedSupporter: false,
      hasPlayedStadium:  false,
      playedPokemonIds:  new Set(),
    };
  }

  private actionForCard(card: GameCard): TurnAction {
    if (card.cardType === 'ENERGY')  return { type: 'ATTACH_ENERGY', energyCardId: card.id, targetInstanceId: '' };
    if (card.cardType === 'TRAINER') return { type: 'PLAY_TRAINER',  cardId: card.id };
    return { type: 'PLAY_POKEMON', cardId: card.id };
  }
}

// ── Helpers puros (sin estado) ────────────────────────────────────────────

function ok(): ValidationResult { return { valid: true }; }
function fail(reason: string): ValidationResult { return { valid: false, reason }; }

function findInHand(pb: PlayerBoard, cardId: string): GameCard | undefined {
  return pb.hand.find(c => c.id === cardId);
}

function findInPlay(pb: PlayerBoard, instanceId: string): PokemonInPlay | undefined {
  if (pb.activePokemon?.instanceId === instanceId) return pb.activePokemon;
  return pb.bench.find(p => p.instanceId === instanceId);
}

/**
 * Regla de evolución: un Pokémon puede evolucionar en otro si el stage
 * siguiente coincide y el nombre base (evolvesFrom) apunta al actual.
 * En ausencia del campo evolvesFrom, se compara por etapa de evolución.
 */
function canEvolveInto(evolution: PokemonCard, base: PokemonCard): boolean {
  const stageOrder: Record<string, number> = { BASIC: 0, EX: 0, STAGE1: 1, STAGE2: 2, MEGA: 3 };
  const evoLevel  = stageOrder[evolution.stage ?? ''] ?? -1;
  const baseLevel = stageOrder[base.stage ?? ''] ?? -1;
  return evoLevel === baseLevel + 1;
}

/**
 * Verifica si un Pokémon tiene suficiente energía para un costo de ataque.
 * Normaliza a mayúsculas para compatibilidad con nombres del API ("Grass" → "GRASS").
 */
function hasEnoughEnergy(pokemon: PokemonInPlay, cost: string[]): boolean {
  const attached = [...(pokemon.attachedEnergies ?? [])].map(e => (e.energyType as string).toUpperCase());
  const remaining = [...attached];

  for (const req of cost) {
    const upper = req.toUpperCase();
    if (upper === 'COLORLESS') continue;
    const idx = remaining.indexOf(upper);
    if (idx === -1) return false;
    remaining.splice(idx, 1);
  }

  const colorlessNeeded = cost.filter(c => c.toUpperCase() === 'COLORLESS').length;
  return remaining.length >= colorlessNeeded;
}

function trainerSubtype(name: string): 'SUPPORTER' | 'STADIUM' | 'ITEM' | 'TOOL' {
  const lower = name.toLowerCase();
  if (SUPPORTER_NAMES.has(lower)) return 'SUPPORTER';
  if (STADIUM_NAMES.has(lower))   return 'STADIUM';
  const toolKws = ['band', 'charm', 'share', 'visor', 'scope', 'brace', 'helmet'];
  if (toolKws.some(kw => lower.includes(kw))) return 'TOOL';
  return 'ITEM';
}
