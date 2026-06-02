import { EnergyType } from '../../shared/models/card.model';

export interface EnergyTypeInfo {
  value: EnergyType;
  label: string;
  color: string;
  cssClass: string;
}

export const ENERGY_TYPES: EnergyTypeInfo[] = [
  { value: 'FIRE',      label: 'Fuego',      color: '#FF6B35', cssClass: 'energy-fire'      },
  { value: 'WATER',     label: 'Agua',       color: '#4FC3F7', cssClass: 'energy-water'     },
  { value: 'GRASS',     label: 'Planta',     color: '#66BB6A', cssClass: 'energy-grass'     },
  { value: 'LIGHTNING', label: 'Rayo',       color: '#FFD54F', cssClass: 'energy-lightning' },
  { value: 'PSYCHIC',   label: 'Psíquica',   color: '#CE93D8', cssClass: 'energy-psychic'   },
  { value: 'FIGHTING',  label: 'Lucha',      color: '#A1887F', cssClass: 'energy-fighting'  },
  { value: 'DARKNESS',  label: 'Oscura',     color: '#546E7A', cssClass: 'energy-darkness'  },
  { value: 'METAL',     label: 'Metálica',   color: '#90A4AE', cssClass: 'energy-metal'     },
  { value: 'FAIRY',     label: 'Hada',       color: '#F48FB1', cssClass: 'energy-fairy'     },
  { value: 'DRAGON',    label: 'Dragón',     color: '#7E57C2', cssClass: 'energy-dragon'    },
  { value: 'COLORLESS', label: 'Incolora',   color: '#BDBDBD', cssClass: 'energy-colorless' },
];

export const ENERGY_TYPE_MAP = new Map<EnergyType, EnergyTypeInfo>(
  ENERGY_TYPES.map(e => [e.value, e])
);
