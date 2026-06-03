import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { EnergyCard, PokemonInPlay } from '../../../../shared/models/game.model';

@Component({
  selector: 'app-energy-attachment',
  standalone: true,
  templateUrl: './energy-attachment.html',
  styleUrl: './energy-attachment.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnergyAttachmentComponent {
  readonly energies = input<EnergyCard[]>([]);
  readonly targets = input<PokemonInPlay[]>([]);
  readonly energyAttached = output<{ energyCardId: string; targetInstanceId: string }>();
  readonly cancelled = output<void>();

  selectedEnergy: EnergyCard | null = null;

  selectEnergy(energy: EnergyCard): void {
    this.selectedEnergy = energy;
  }

  selectTarget(target: PokemonInPlay): void {
    if (this.selectedEnergy) {
      this.energyAttached.emit({
        energyCardId: this.selectedEnergy.id,
        targetInstanceId: target.instanceId
      });
      this.selectedEnergy = null;
    }
  }
}
