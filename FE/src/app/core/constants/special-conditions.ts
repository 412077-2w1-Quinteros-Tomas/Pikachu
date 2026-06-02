export type SpecialCondition = 'ASLEEP' | 'BURNED' | 'CONFUSED' | 'PARALYZED' | 'POISONED';

export interface SpecialConditionInfo {
  value: SpecialCondition;
  label: string;
  description: string;
  color: string;
  cssClass: string;
  /** Condiciones con las que NO puede coexistir. */
  exclusiveWith: SpecialCondition[];
}

export const SPECIAL_CONDITIONS: SpecialConditionInfo[] = [
  {
    value: 'ASLEEP',
    label: 'Dormido',
    description: 'Flip de moneda entre turnos: cara = despierta, cruz = sigue dormido y saltea el turno.',
    color: '#7986CB',
    cssClass: 'condition-asleep',
    exclusiveWith: ['CONFUSED', 'PARALYZED'],
  },
  {
    value: 'BURNED',
    label: 'Quemado',
    description: 'Entre turnos: 2 contadores de daño. Flip de moneda: cara = se cura.',
    color: '#EF5350',
    cssClass: 'condition-burned',
    exclusiveWith: [],
  },
  {
    value: 'CONFUSED',
    label: 'Confundido',
    description: 'Al atacar: flip de moneda. Cruz = 30 de auto-daño y el ataque falla.',
    color: '#AB47BC',
    cssClass: 'condition-confused',
    exclusiveWith: ['ASLEEP', 'PARALYZED'],
  },
  {
    value: 'PARALYZED',
    label: 'Paralizado',
    description: 'No puede atacar ni retirarse. Se quita al final del próximo turno.',
    color: '#FFA726',
    cssClass: 'condition-paralyzed',
    exclusiveWith: ['ASLEEP', 'CONFUSED'],
  },
  {
    value: 'POISONED',
    label: 'Envenenado',
    description: 'Entre turnos: 1 contador de daño.',
    color: '#66BB6A',
    cssClass: 'condition-poisoned',
    exclusiveWith: [],
  },
];

export const SPECIAL_CONDITION_MAP = new Map<SpecialCondition, SpecialConditionInfo>(
  SPECIAL_CONDITIONS.map(c => [c.value, c])
);
