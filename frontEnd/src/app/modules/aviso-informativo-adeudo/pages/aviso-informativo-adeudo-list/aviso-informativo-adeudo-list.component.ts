import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import Swal from 'sweetalert2';
import { AvisoInformativoAdeudoService } from '../../../shared/services/aviso-informativo-adeudo.service';
import { UserService } from '../../../shared/services/user.service';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoInformativoAdeudoModel, UsuarioInformativoAdeudoModel } from '../../../shared/models/AvisoInformativoAdeudo.model';
import { AvisoInformativoAdeudoEntregaDialogComponent } from '../../components/aviso-informativo-adeudo-entrega-dialog/aviso-informativo-adeudo-entrega-dialog.component';

// Pantalla de "Aviso Informativo de Adeudo" -- notificación PREVIA y más
// suave que las Cartas de adeudo, SOLO para usuarios elegidos a mano por el
// Comité (ver clarificación de Ely: "segun yo te pedi un modulo aparte...
// solo es para ciertos usuarios"). A diferencia de Cartas de adeudo, NO hay
// tabla de candidatos con cálculo automático: se busca al usuario por
// nombre/número (mismo autocomplete que ya usa "usuario manual" en Cartas
// de adeudo) y se capturan a mano los datos de adeudo que se quieran
// mostrar en el aviso. Folio propio, totalmente independiente del de
// Cartas de adeudo.
@Component({
  selector: 'app-aviso-informativo-adeudo-list',
  templateUrl: './aviso-informativo-adeudo-list.component.html',
  styleUrls: ['./aviso-informativo-adeudo-list.component.css']
})
export class AvisoInformativoAdeudoListComponent implements OnInit {

  private readonly avisoInformativoAdeudoService = inject(AvisoInformativoAdeudoService);
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  historialColumns: string[] = [
    'folioNotificacion', 'noUsuario', 'nombreUsuarioTitular', 'noCasaTexto', 'periodosAdeudados',
    'multaAcumulada', 'fechaPresentacion', 'dateAdd', 'entrega', 'acciones'
  ];

  // "Debe presentarse el día ___" -- se pide siempre antes de generar, para
  // poder darle seguimiento (pedido explícito de Ely). hoyStr es el mínimo
  // permitido en el selector, mismo criterio que Cartas de adeudo/Padron.
  fechaPresentacion: string | null = null;
  readonly hoyStr = new Date().toISOString().substring(0, 10);

  tiposEntrega = TIPOS_ENTREGA;

  // Búsqueda del usuario a agregar -- mismo autocomplete que ya usa la
  // sección de "usuario manual" en Cartas de adeudo.
  userSearchCtrl = new FormControl('');
  userSearchResults: { aguaUsuarioId: number; noUsuario: number; nombreCompleto: string }[] = [];
  usuarioSeleccionado: { aguaUsuarioId: number; noUsuario: number; nombreCompleto: string } | null = null;

  // Nota libre del Comité para el usuario que se está por agregar -- lo
  // único que se captura a mano (el adeudo se calcula en el backend).
  nuevoObservacion = '';

  // Lista armada, lista para generar.
  usuarios: UsuarioInformativoAdeudoModel[] = [];

  generando = false;

  historial: AvisoInformativoAdeudoModel[] = [];
  cargandoHistorial = false;
  mostrarCanceladas = false;

  get historialVisible(): AvisoInformativoAdeudoModel[] {
    return this.mostrarCanceladas ? this.historial : this.historial.filter(a => !a.cancelado);
  }

  etiquetaTipoEntrega(valor: string | undefined): string {
    return this.tiposEntrega.find(t => t.valor === valor)?.etiqueta ?? valor ?? '';
  }

  formatearFechaNaive(fechaISO: string | undefined): string {
    if (!fechaISO) return '';
    const [anio, mes, dia] = fechaISO.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  // Se abre en pestaña nueva (mismo criterio que Cartas de adeudo) para no
  // perder el filtro/selección en curso en esta pantalla.
  irADetalleUsuario(aguaUsuarioId?: number): void {
    if (!aguaUsuarioId) return;
    const urlTree = this.router.createUrlTree(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: aguaUsuarioId }) }
    });
    window.open(this.router.serializeUrl(urlTree), '_blank');
  }

  ngOnInit(): void {
    this.cargarHistorial();

    this.userSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const value = typeof term === 'string' ? term.trim() : '';
        if (value.length < 2) return of([]);
        return this.userService.searchUsersByNumber(value);
      })
    ).subscribe({
      next: (results: any) => this.userSearchResults = results || [],
      error: () => this.userSearchResults = []
    });
  }

  displayUsuario = (usuario: { noUsuario: number; nombreCompleto: string }): string => {
    return usuario?.nombreCompleto ? `N° ${usuario.noUsuario} - ${usuario.nombreCompleto}` : '';
  };

  onUsuarioSeleccionado(event: MatAutocompleteSelectedEvent): void {
    this.usuarioSeleccionado = event.option.value;
  }

  agregarUsuario(): void {
    if (!this.usuarioSeleccionado) {
      this.openSnackBar('Busca y elige el usuario primero', 'Atención');
      return;
    }
    if (this.usuarios.some(u => u.aguaUsuarioId === this.usuarioSeleccionado!.aguaUsuarioId)) {
      this.openSnackBar('Ese usuario ya está en la lista', 'Atención');
      return;
    }

    this.usuarios.push({
      aguaUsuarioId: this.usuarioSeleccionado.aguaUsuarioId,
      noUsuario: this.usuarioSeleccionado.noUsuario,
      nombreCompleto: this.usuarioSeleccionado.nombreCompleto,
      observacion: this.nuevoObservacion.trim() || undefined
    });

    this.usuarioSeleccionado = null;
    this.userSearchCtrl.setValue('');
    this.userSearchResults = [];
    this.nuevoObservacion = '';
  }

  quitarUsuario(index: number): void {
    this.usuarios.splice(index, 1);
  }

  generarAvisos(): void {
    if (this.usuarios.length === 0) {
      this.openSnackBar('Agrega al menos un usuario a la lista', 'Atención');
      return;
    }
    if (!this.fechaPresentacion) {
      this.openSnackBar('Indica la fecha en que se les invita a presentarse antes de generar', 'Atención');
      return;
    }
    if (this.fechaPresentacion < this.hoyStr) {
      this.openSnackBar('La fecha de presentación no puede ser anterior a hoy', 'Atención');
      return;
    }

    Swal.fire({
      title: `¿Generar Aviso Informativo de Adeudo para ${this.usuarios.length} usuario(s)?`,
      html: 'Se les asigna folio consecutivo propio (independiente del de Cartas de adeudo) y queda registrado en el historial.',
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
    this.avisoInformativoAdeudoService.generar(this.usuarios, this.fechaPresentacion!).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, 'avisos_informativos_adeudo.pdf');
        this.openSnackBar('Avisos generados correctamente', 'Éxito');

        this.usuarios = [];
        this.fechaPresentacion = null;
        this.historial = [];
        this.cargarHistorial();
      },
      error: (e: any) => {
        this.generando = false;
        console.error(e);
        const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudieron generar los avisos';
        this.openSnackBar(mensaje, 'Error');
      }
    });
  }

  private cargarHistorial(): void {
    this.cargandoHistorial = true;
    this.avisoInformativoAdeudoService.getHistorial().subscribe({
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

  registrarEntrega(aviso: AvisoInformativoAdeudoModel): void {
    const dialogRef = this.dialog.open(AvisoInformativoAdeudoEntregaDialogComponent, {
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

  verEvidenciaFotografica(aviso: AvisoInformativoAdeudoModel): void {
    this.dialog.open(AvisoInformativoAdeudoEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  cancelarAviso(aviso: AvisoInformativoAdeudoModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: `${aviso.noUsuario} - ${aviso.nombreUsuarioTitular}. Se oculta del historial, pero se puede consultar con "Mostrar cancelados".`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoInformativoAdeudoService.cancelar(aviso.avisoInformativoAdeudoId).subscribe({
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

  reactivarAviso(aviso: AvisoInformativoAdeudoModel): void {
    this.avisoInformativoAdeudoService.reactivar(aviso.avisoInformativoAdeudoId).subscribe({
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
