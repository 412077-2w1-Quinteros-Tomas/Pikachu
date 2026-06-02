import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'energyType' })
export class EnergyTypePipe implements PipeTransform {
  transform(value: unknown): unknown { return value; }
}
