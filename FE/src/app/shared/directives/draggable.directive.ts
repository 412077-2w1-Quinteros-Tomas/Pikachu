import { Directive, ElementRef, output, inject } from '@angular/core';

@Directive({
  selector: '[appDraggable]',
  host: {
    '[draggable]': 'true',
    '(dragstart)': 'onDragStart($event)',
    '(dragend)': 'onDragEnd($event)',
  },
})
export class DraggableDirective {

  readonly dragStarted = output<DragEvent>();
  readonly dragEnded = output<DragEvent>();

  private readonly el = inject(ElementRef);

  onDragStart(event: DragEvent): void {
    this.el.nativeElement.classList.add('dragging');
    this.dragStarted.emit(event);
  }

  onDragEnd(event: DragEvent): void {
    this.el.nativeElement.classList.remove('dragging');
    this.dragEnded.emit(event);
  }
}
