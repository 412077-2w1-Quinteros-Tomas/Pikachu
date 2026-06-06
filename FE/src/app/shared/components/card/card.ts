import { Component, ChangeDetectionStrategy, input, output, ElementRef, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModel } from '../../models/card.model';
import { ENERGY_TYPE_MAP } from '../../../core/constants/energy-types';

const TYPE_COLORS: Record<string, string> = {
  FIRE: '#ff5533', WATER: '#3388ff', GRASS: '#44bb44',
  LIGHTNING: '#ffdd00', PSYCHIC: '#cc44cc', FIGHTING: '#cc6600',
  DARKNESS: '#334466', METAL: '#999999', FAIRY: '#ff88bb',
  DRAGON: '#7755cc', COLORLESS: '#aaaaaa'
};

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './card.html',
  styleUrl: './card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CardComponent {
  card     = input.required<CardModel>();
  selected = input(false);
  compact  = input(false);
  clicked  = output<CardModel>();

  readonly energyTypeMap = ENERGY_TYPE_MAP;
  private readonly el = inject(ElementRef).nativeElement as HTMLElement;

  onCardClick(): void {
    this.clicked.emit(this.card());
  }

  getEnergyColor(type: string): string {
    return TYPE_COLORS[type] ?? '#BDBDBD';
  }

  getPrimaryTypeColor(): string {
    const types = this.card().types;
    return types?.length ? (TYPE_COLORS[types[0]] ?? '#7c83fd') : '#7c83fd';
  }

  getRarityStars(): string {
    const r = this.card().rarity ?? '';
    if (r.includes('Rare Holo')) return '◆◆◆';
    if (r.includes('Rare')) return '◆◆';
    if (r.includes('Uncommon')) return '◆';
    return '';
  }

  @HostListener('mousemove', ['$event'])
  onMouseMove(e: MouseEvent): void {
    const rect = this.el.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const cx = rect.width  / 2;
    const cy = rect.height / 2;
    const rx = ((y - cy) / cy) * -14;
    const ry = ((x - cx) / cx) *  14;
    const px = (x / rect.width)  * 100;
    const py = (y / rect.height) * 100;

    const s = this.el.style;
    s.setProperty('--rx', `${rx}deg`);
    s.setProperty('--ry', `${ry}deg`);
    s.setProperty('--mx', `${px}%`);
    s.setProperty('--my', `${py}%`);
    s.setProperty('--hx', `${50 + (px - 50) * 0.4}%`);
    s.setProperty('--hy', `${50 + (py - 50) * 0.4}%`);
    this.el.classList.add('is-hovering');
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    const s = this.el.style;
    s.setProperty('--rx', '0deg');
    s.setProperty('--ry', '0deg');
    this.el.classList.remove('is-hovering');
  }
}
