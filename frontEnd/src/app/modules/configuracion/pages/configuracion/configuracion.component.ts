import { Component, OnInit, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { ConfiguracionService } from '../../../shared/services/configuracion.service';
import { CLAVE_BLOQUEADO, CLAVE_NOMBRE_COMITE, ConfiguracionModel } from '../../../shared/models/Configuracion.model';

@Component({
  selector: 'app-configuracion',
  templateUrl: './configuracion.component.html',
  styleUrls: ['./configuracion.component.css']
})
export class ConfiguracionComponent implements OnInit {

  private readonly configuracionService = inject(ConfiguracionService);
  private readonly snackBar = inject(MatSnackBar);

  cargando = false;
  guardandoNombreComite = false;

  nombreComite = '';
  bloqueado = false;

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando = true;
    this.configuracionService.getAll().subscribe({
      next: (resp: any) => {
        const lista: ConfiguracionModel[] = resp?.data ?? [];
        this.nombreComite = lista.find(c => c.clave === CLAVE_NOMBRE_COMITE)?.valor ?? 'Los Lopez';
        this.bloqueado = (lista.find(c => c.clave === CLAVE_BLOQUEADO)?.valor ?? 'false') === 'true';
        this.cargando = false;
      },
      error: (e) => {
        console.error(e);
        this.cargando = false;
        this.openSnackBar('No se pudieron cargar las configuraciones', 'Error');
      }
    });
  }

  guardarNombreComite(): void {
    if (!this.nombreComite.trim()) {
      this.openSnackBar('Ponle un nombre al comité', 'Atención');
      return;
    }
    this.guardandoNombreComite = true;
    this.configuracionService.updateByClave(
      CLAVE_NOMBRE_COMITE,
      this.nombreComite.trim(),
      'Nombre del comité que aparece en el título de la app'
    ).subscribe({
      next: () => {
        this.guardandoNombreComite = false;
        this.openSnackBar('Nombre del comité guardado', 'Éxito');
      },
      error: (e) => {
        console.error(e);
        this.guardandoNombreComite = false;
        this.openSnackBar(e?.error?.metadata?.detail || 'No se pudo guardar el nombre del comité', 'Error');
      }
    });
  }

  // El switch ya cambió de valor en pantalla (ngModel) antes de que se
  // dispare este evento -- por eso, si se cancela la confirmación, hay que
  // regresarlo a mano a como estaba.
  onToggleBloqueado(nuevoValor: boolean): void {
    const titulo = nuevoValor ? '¿Bloquear las configuraciones?' : '¿Desbloquear las configuraciones?';
    const texto = nuevoValor
      ? 'Ya no se van a poder editar hasta que las desbloquees de nuevo.'
      : 'Se van a poder editar de nuevo.';
    Swal.fire({
      title: titulo,
      text: texto,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        this.bloqueado = !nuevoValor;
        return;
      }
      this.configuracionService.updateByClave(
        CLAVE_BLOQUEADO,
        nuevoValor ? 'true' : 'false',
        'Si está en "true", ya no se pueden editar las demás configuraciones'
      ).subscribe({
        next: () => {
          this.bloqueado = nuevoValor;
          this.openSnackBar(nuevoValor ? 'Configuraciones bloqueadas' : 'Configuraciones desbloqueadas', 'Éxito');
        },
        error: (e) => {
          console.error(e);
          this.bloqueado = !nuevoValor;
          this.openSnackBar('No se pudo cambiar el bloqueo', 'Error');
        }
      });
    });
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 3000 });
  }
}
