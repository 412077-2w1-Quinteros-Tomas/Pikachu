import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = error.error?.message ?? error.message ?? 'Error desconocido';

      switch (error.status) {
        case 0:
          console.error('Sin conexión con el servidor:', message);
          break;
        case 404:
          console.error('Recurso no encontrado:', message);
          break;
        case 409:
          console.error('Acción inválida:', message);
          break;
        case 500:
          console.error('Error interno del servidor:', message);
          break;
        default:
          console.error(`Error HTTP ${error.status}:`, message);
      }

      return throwError(() => ({ status: error.status, message }));
    })
  );
};
