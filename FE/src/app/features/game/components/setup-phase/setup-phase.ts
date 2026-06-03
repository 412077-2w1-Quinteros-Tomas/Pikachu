import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-setup-phase',
  standalone: true,
  templateUrl: './setup-phase.html',
  styleUrl: './setup-phase.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SetupPhaseComponent {
  readonly message = input<string>('Esperando al oponente...');
}
