import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-status-indicator',
  templateUrl: './status-indicator.html',
  styleUrl: './status-indicator.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatusIndicatorComponent {}
