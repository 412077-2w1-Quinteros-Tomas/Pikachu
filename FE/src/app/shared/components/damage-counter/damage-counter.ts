import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-damage-counter',
  templateUrl: './damage-counter.html',
  styleUrl: './damage-counter.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DamageCounterComponent {}
