import { Component, ChangeDetectionStrategy, input, OnChanges } from '@angular/core';
import { GameEvent } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-notification-toast',
  standalone: true,
  templateUrl: './notification-toast.html',
  styleUrl: './notification-toast.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotificationToastComponent implements OnChanges {
  readonly events = input<GameEvent[]>([]);

  visible = false;
  currentMessage = '';

  private timer: ReturnType<typeof setTimeout> | null = null;

  ngOnChanges(): void {
    const evts = this.events();
    if (!evts || evts.length === 0) return;
    const last = evts[evts.length - 1];
    this.currentMessage = this.formatEvent(last);
    this.visible = true;

    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(() => { this.visible = false; }, 3000);
  }

  private formatEvent(evt: GameEvent): string {
    const data = evt.data ?? {};
    switch (evt.type) {
      case 'ATTACK_PERFORMED': return `⚔️ ${data['attacker']} usó un ataque`;
      case 'DAMAGE_DEALT': return `💥 ${data['damage']} daño a ${data['target']}`;
      case 'POKEMON_KO': return `☠️ ${data['pokemon']} fue noqueado`;
      case 'PRIZE_TAKEN': return `🎴 ¡Premio tomado! (${data['prizeCardsLeft']} restantes)`;
      case 'STATUS_APPLIED': return `${data['condition']} aplicado a ${data['target']}`;
      case 'STATUS_REMOVED': return `${data['condition']} curado en ${data['pokemon']}`;
      case 'ENERGY_ATTACHED': return `⚡ Energía adjuntada a ${data['target']}`;
      case 'TURN_STARTED': return `▶️ Turno ${data['turnNumber']}`;
      case 'GAME_OVER': return `🏆 ¡Ganador: ${data['winner']}!`;
      default: return evt.type;
    }
  }
}
