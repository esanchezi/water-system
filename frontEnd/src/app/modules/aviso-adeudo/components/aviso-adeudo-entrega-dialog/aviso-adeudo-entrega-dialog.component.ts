import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AvisoAdeudoEntregaModel, AvisoAdeudoModel, TIPOS_ENTREGA_ADEUDO } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoAdeudoFotoModel } from '../../../shared/models/AvisoAdeudoFoto.model';
import { AvisoAdeudoService } from '../../../shared/services/aviso-adeudo.service';
import { AvisoAdeudoFotoService } from '../../../shared/services/aviso-adeudo-foto.service';
import Swal from 'sweetalert2';

export interface AvisoAdeudoEntregaDialogData {
  aviso: AvisoAdeudoModel;
  // true cuando se reabre este mismo dialog SOLO para consultar (y en su
  // caso agregar/quitar) las fotos de respaldo de una entrega YA
  // registrada -- la entrega en sí ya no se vuelve a capturar (el botón
  // "Marcar como entregada" desaparece del historial en cuanto entregado
  // = true). Ver AvisoAdeudoListComponent, botón "Ver evidencia fotográfica".
  soloVerFotos?: boolean;
}

// Captura la sección "RAZÓN DE NOTIFICACIÓN" de la carta física, para
// registrar en el sistema qué pasó al momento de entregarla: quién recibió
// (o si se negaron a recibir / no encontraron a nadie), quién la entregó
// por el Comité, y los testigos cuando aplica. También permite adjuntar
// fotos de respaldo (evidencia de que se buscó al usuario) cuando no se
// le encontró -- se suben de inmediato, ligadas al avisoAdeudoId que ya
// existe (la carta ya se había generado antes de abrir este dialog).
@Component({
  selector: 'app-aviso-adeudo-entrega-dialog',
  templateUrl: './aviso-adeudo-entrega-dialog.component.html',
  styleUrls: ['./aviso-adeudo-entrega-dialog.component.css']
})
export class AvisoAdeudoEntregaDialogComponent implements OnInit {

  tiposEntrega = TIPOS_ENTREGA_ADEUDO;

  tipoEntrega = '';
  fechaEntrega = new Date().toISOString().substring(0, 10);
  // Hora de entrega -- la carta física trae "siendo las ______ horas" en la
  // sección RAZÓN DE NOTIFICACIÓN, así que se captura junto con la fecha en
  // vez de solo guardar la fecha con hora en 00:00 (como antes). Se
  // precarga con la hora actual, pero se puede ajustar a mano si se está
  // registrando después de la visita real.
  horaEntrega = new Date().toTimeString().substring(0, 5);
  nombreReceptor = '';
  parentescoReceptor = '';
  nombreNotificador = '';
  nombreTestigo1 = '';
  nombreTestigo2 = '';

  guardando = false;
  error = '';

  // Fotos de respaldo (cuando no se encontró al usuario o se negó a firmar)
  fotos: AvisoAdeudoFotoModel[] = [];
  fotoUrls: { [fotoId: number]: string } = {};
  subiendoFoto = false;

  // Comentario libre para explicar qué sucedió (opcional)
  comentarioEntrega = '';

  // Solo aplica cuando tipoEntrega = ABONO -- folio del recibo ya
  // capturado en Pagos, como soporte del abono.
  folioReciboVinculado: number | null = null;

  get requiereFotos(): boolean {
    return this.tipoEntrega === 'NO_ENCONTRADO' || this.tipoEntrega === 'SE_NEGO' || this.data.soloVerFotos === true;
  }

  get esAbono(): boolean {
    return this.tipoEntrega === 'ABONO';
  }

  get requiereReceptor(): boolean {
    return this.tipoEntrega === 'OTRA_PERSONA';
  }

  // Solo cuando se negaron a recibir o no encontraron a nadie hace falta
  // constancia de testigos (igual que en la carta física).
  get requiereTestigos(): boolean {
    return this.tipoEntrega === 'SE_NEGO' || this.tipoEntrega === 'NO_ENCONTRADO';
  }

  get formularioValido(): boolean {
    if (!this.tipoEntrega || !this.nombreNotificador.trim()) return false;
    if (this.requiereReceptor && (!this.nombreReceptor.trim() || !this.parentescoReceptor.trim())) return false;
    if (this.requiereTestigos && !this.nombreTestigo1.trim()) return false;
    return true;
  }

  constructor(
    public dialogRef: MatDialogRef<AvisoAdeudoEntregaDialogComponent>,
    private readonly avisoAdeudoService: AvisoAdeudoService,
    private readonly avisoAdeudoFotoService: AvisoAdeudoFotoService,
    @Inject(MAT_DIALOG_DATA) public data: AvisoAdeudoEntregaDialogData
  ) { }

  ngOnInit(): void {
    this.cargarFotos();
  }

  private cargarFotos(): void {
    this.avisoAdeudoFotoService.listarPorAviso(this.data.aviso.avisoAdeudoId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00') return;
        this.fotos = resp.data || [];
        this.fotos.forEach(f => {
          if (this.fotoUrls[f.fotoId]) return;
          this.avisoAdeudoFotoService.getArchivoBlob(f.fotoId).subscribe({
            next: (blob: Blob) => this.fotoUrls[f.fotoId] = URL.createObjectURL(blob),
            error: (e: any) => console.error('Error al cargar foto', e)
          });
        });
      },
      error: (e: any) => console.error('Error al consultar fotos de la entrega', e)
    });
  }

  subirFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    this.subiendoFoto = true;
    this.avisoAdeudoFotoService.subirFoto(this.data.aviso.avisoAdeudoId, archivo).subscribe({
      next: () => {
        this.subiendoFoto = false;
        input.value = '';
        this.cargarFotos();
      },
      error: (e: any) => {
        this.subiendoFoto = false;
        input.value = '';
        console.error(e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo subir la foto.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  eliminarFoto(foto: AvisoAdeudoFotoModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Eliminar foto',
      text: '¿Confirmas eliminar esta foto de respaldo?',
      showCancelButton: true,
      confirmButtonText: 'Eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoAdeudoFotoService.eliminarFoto(foto.fotoId).subscribe({
        next: () => {
          if (this.fotoUrls[foto.fotoId]) {
            URL.revokeObjectURL(this.fotoUrls[foto.fotoId]);
            delete this.fotoUrls[foto.fotoId];
          }
          this.cargarFotos();
        },
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo eliminar la foto.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }

  guardar(): void {
    if (!this.formularioValido) return;
    this.guardando = true;
    this.error = '';

    const datos: AvisoAdeudoEntregaModel = {
      tipoEntrega: this.tipoEntrega,
      nombreReceptor: this.requiereReceptor ? this.nombreReceptor.trim() : undefined,
      parentescoReceptor: this.requiereReceptor ? this.parentescoReceptor.trim() : undefined,
      nombreNotificador: this.nombreNotificador.trim(),
      nombreTestigo1: this.nombreTestigo1.trim() || undefined,
      nombreTestigo2: this.nombreTestigo2.trim() || undefined,
      comentarioEntrega: this.comentarioEntrega.trim() || undefined,
      folioReciboVinculado: this.esAbono && this.folioReciboVinculado ? this.folioReciboVinculado : undefined,
      // OJO: se manda como fecha/hora "naive" (sin Z ni offset) a propósito
      // -- convertir con new Date(...).toISOString() la anclaba en UTC
      // medianoche, y al mostrarla de vuelta en la zona horaria local
      // (México, UTC-6) aparecía un día antes. Mandándola tal cual se
      // evita esa conversión de un lado a otro. La hora ya no se deja fija
      // en 00:00 -- se captura aparte (horaEntrega) para poder replicar el
      // "siendo las ______ horas" de la carta física.
      fechaEntrega: this.fechaEntrega ? this.fechaEntrega + 'T' + (this.horaEntrega || '00:00') + ':00' : undefined
    };

    this.avisoAdeudoService.marcarEntregada(this.data.aviso.avisoAdeudoId, datos).subscribe({
      next: () => {
        this.guardando = false;
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        this.guardando = false;
        this.error = 'No se pudo registrar la entrega, intenta de nuevo.';
        console.error(e);
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
