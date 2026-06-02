export type GamePhase =
  | 'WAITING' | 'SETUP' | 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS' | 'FINISHED';

export type TurnPhase = 'DRAW' | 'ACTIONS' | 'ATTACK' | 'BETWEEN_TURNS';

export interface GamePhaseInfo {
  value: GamePhase;
  label: string;
  description: string;
}

export interface TurnPhaseInfo {
  value: TurnPhase;
  label: string;
  description: string;
}

export const GAME_PHASES: GamePhaseInfo[] = [
  { value: 'WAITING',       label: 'Esperando',      description: 'Esperando al segundo jugador'           },
  { value: 'SETUP',         label: 'Configuración',  description: 'Colocando Pokémon iniciales'            },
  { value: 'DRAW',          label: 'Robo',           description: 'Robando carta del mazo'                 },
  { value: 'MAIN',          label: 'Acciones',       description: 'Realizando acciones del turno'          },
  { value: 'ATTACK',        label: 'Ataque',         description: 'Declarando ataque'                      },
  { value: 'BETWEEN_TURNS', label: 'Entre turnos',   description: 'Procesando efectos entre turnos'        },
  { value: 'FINISHED',      label: 'Finalizada',     description: 'La partida ha terminado'                },
];

export const TURN_PHASES: TurnPhaseInfo[] = [
  { value: 'DRAW',          label: 'Robo',         description: 'Roba 1 carta de tu mazo'                 },
  { value: 'ACTIONS',       label: 'Acciones',     description: 'Coloca Pokémon, attachá energía y más'   },
  { value: 'ATTACK',        label: 'Ataque',       description: 'Declará un ataque con tu Pokémon activo' },
  { value: 'BETWEEN_TURNS', label: 'Entre turnos', description: 'Efectos de condiciones especiales'       },
];

export const GAME_PHASE_MAP = new Map<GamePhase, GamePhaseInfo>(
  GAME_PHASES.map(p => [p.value, p])
);
