import { Component, OnInit, inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import Swal from 'sweetalert2';
import { UserService } from '../../../shared/services/user.service';
import { AvisoMedidorService } from '../../../shared/services/aviso-medidor.service';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoMedidorModel } from '../../../shared/models/AvisoMedidor.model';
import { AvisoMedidorEntregaDialogComponent } from '../../components/aviso-medidor-entrega-dialog/aviso-medidor-entrega-dialog.component';

// Control de "Aviso de Instalación de Medidor" (prueba de consumo, Art. 7)
// -- a diferencia de Cartas de adeudo/Aviso de Bomba, aquí NO hay
// Primero/Segundo ni cálculo de candidatos: el Comité elige a mano un solo
// domicilio de prueba (autocomplete por N° de usuario/nombre, mismo
// patrón que "Usuario manual" en Cartas de adeudo) y captura los datos
// propios de la instalación (fecha, número de serie, lectura inicial).
@Component({
  selector: 'app-aviso-medidor-list',
  templateUrl: './aviso-medidor-list.component.html',
  styleUrls: ['./aviso-medidor-list.component.css']
})
export class AvisoMedidorListComponent implements OnInit {

  private readonly userService = inject(UserService);
  private readonly avisoMedidorService = inject(AvisoMedidorService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  tiposEntrega = TIPOS_ENTREGA;

  historialColumns: string[] = [
    'folioNotificacion', 'noUsuario', 'nombreUsuarioTitular', 'noCasaTexto', 'domicilioToma',
    'fechaInstalacion', 'noSerieMedidor', 'lecturaInicialMedidor', 'dateAdd', 'entrega', 'acciones'
  ];

  // Autocomplete de usuario -- mismo patrón que "Usuario manual" en
  // AvisoAdeudoListComponent.
  usuarioSearchCtrl = new FormControl('');
  usuarioSearchResults: any[] = [];
  usuarioSeleccionado: { aguaUsuarioId: number; noUsuario: number; nombreCompleto: string } | null = null;

  fechaInstalacion: string | null = null;
  noSerieMedidor = '';
  lecturaInicialMedidor = '';

  generando = false;

  historial: AvisoMedidorModel[] = [];
  cargandoHistorial = false;
  mostrarCanceladas = false;

  get historialVisible(): AvisoMedidorModel[] {
    return this.mostrarCanceladas ? this.historial : this.historial.filter(a => !a.cancelado);
  }

  get formularioValido(): boolean {
    return !!this.usuarioSeleccionado && !!this.fechaInstalacion && this.noSerieMedidor.trim().length > 0;
  }

  etiquetaTipoEntrega(valor: string | undefined): string {
    return this.tiposEntrega.find(t => t.valor === valor)?.etiqueta ?? valor ?? '';
  }

  // "dd/MM/yyyy" armado con puro texto -- ver mismo criterio en
  // AvisoAdeudoListComponent.formatearFechaNaive().
  formatearFechaNaive(fechaISO: string | undefined): string {
    if (!fechaISO) return '';
    const [anio, mes, dia] = fechaISO.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  ngOnInit(): void {
    this.cargarHistorial();

    this.usuarioSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const value = typeof term === 'string' ? term.trim() : '';
        if (value.length < 2) return of([]);
        return this.userService.searchUsersByNumber(value);
      })
    ).subscribe({
      next: (results: any) => this.usuarioSearchResults = results || [],
      error: () => this.usuarioSearchResults = []
    });
  }

  displayUsuario = (usuario: { noUsuario: number; nombreCompleto: string }): string => {
    return usuario?.nombreCompleto ? `N° ${usuario.noUsuario} - ${usuario.nombreCompleto}` : '';
  };

  onUsuarioSeleccionado(event: MatAutocompleteSelectedEvent): void {
    this.usuarioSeleccionado = event.option.value;
  }

  // Link directo a la ficha completa del usuario -- se abre en pestaña
  // nueva (mismo criterio que el resto de avisos) para no perder lo que se
  // esté capturando en esta pantalla.
  irADetalleUsuario(aguaUsuarioId?: number): void {
    if (!aguaUsuarioId) return;
    const urlTree = this.router.createUrlTree(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: aguaUsuarioId }) }
    });
    window.open(this.router.serializeUrl(urlTree), '_blank');
  }

  private cargarHistorial(): void {
    this.cargandoHistorial = true;
    this.avisoMedidorService.getHistorial().subscribe({
      next: (resp: any) => {
        this.cargandoHistorial = false;
        if (resp.metadata?.code === '00') {
          this.historial = resp.data || [];
        }
      },
      error: (e: any) => {
        this.cargandoHistorial = false;
        console.error(e);
      }
    });
  }

  generarAviso(): void {
    if (!this.formularioValido || !this.usuarioSeleccionado) {
      this.openSnackBar('Selecciona el usuario, la fecha de instalación y el número de serie', 'Atención');
      return;
    }

    Swal.fire({
      title: `¿Generar Aviso de Instalación de Medidor?`,
      html: `N° ${this.usuarioSeleccionado.noUsuario} - ${this.usuarioSeleccionado.nombreCompleto}. Se le asigna folio consecutivo y queda registrado en el historial.`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, generar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.confirmarGeneracion();
    });
  }

  private confirmarGeneracion(): void {
    this.generando = true;
    this.avisoMedidorService.generar(
      this.usuarioSeleccionado!.aguaUsuarioId,
      this.fechaInstalacion!,
      this.noSerieMedidor.trim(),
      this.lecturaInicialMedidor.trim()
    ).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, 'aviso_instalacion_medidor.pdf');
        this.openSnackBar('Aviso generado correctamente', 'Éxito');

        this.usuarioSeleccionado = null;
        this.usuarioSearchCtrl.setValue('');
        this.fechaInstalacion = null;
        this.noSerieMedidor = '';
        this.lecturaInicialMedidor = '';
        this.cargarHistorial();
      },
      error: (e: any) => {
        this.generando = false;
        console.error(e);
        const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudo generar el aviso';
        this.openSnackBar(mensaje, 'Error');
      }
    });
  }

  registrarEntrega(aviso: AvisoMedidorModel): void {
    const dialogRef = this.dialog.open(AvisoMedidorEntregaDialogComponent, {
      width: '480px',
      data: { aviso }
    });
    dialogRef.afterClosed().subscribe((guardado: boolean) => {
      if (guardado) {
        this.openSnackBar('Entrega registrada', 'Éxito');
        this.cargarHistorial();
      }
    });
  }

  // Reabre el mismo dialog solo para consultar/agregar/quitar las fotos de
  // respaldo de una entrega ya registrada -- mismo patrón que Aviso de
  // Bomba/Cartas de adeudo.
  verEvidenciaFotografica(aviso: AvisoMedidorModel): void {
    this.dialog.open(AvisoMedidorEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  cancelarAviso(aviso: AvisoMedidorModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: `${aviso.noUsuario} - ${aviso.nombreUsuarioTitular}. Se oculta del historial, pero se puede consultar con "Mostrar cancelados".`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoMedidorService.cancelar(aviso.avisoMedidorId).subscribe({
        next: () => {
          this.openSnackBar('Aviso cancelado', 'Éxito');
          this.cargarHistorial();
        },
        error: (e: any) => {
          console.error(e);
          this.openSnackBar('No se pudo cancelar el aviso', 'Error');
        }
      });
    });
  }

  reactivarAviso(aviso: AvisoMedidorModel): void {
    this.avisoMedidorService.reactivar(aviso.avisoMedidorId).subscribe({
      next: () => {
        this.openSnackBar('Aviso reactivado', 'Éxito');
        this.cargarHistorial();
      },
      error: (e: any) => {
        console.error(e);
        this.openSnackBar('No se pudo reactivar el aviso', 'Error');
      }
    });
  }

  private descargarBlob(blob: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 4000 });
  }
}
