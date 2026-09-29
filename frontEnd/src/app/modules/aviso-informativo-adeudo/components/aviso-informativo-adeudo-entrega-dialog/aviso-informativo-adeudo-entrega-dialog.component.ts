import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoInformativoAdeudoEntregaModel, AvisoInformativoAdeudoModel } from '../../../shared/models/AvisoInformativoAdeudo.model';
import { AvisoInformativoAdeudoFotoModel } from '../../../shared/models/AvisoInformativoAdeudoFoto.model';
import { AvisoInformativoAdeudoService } from '../../../shared/services/aviso-informativo-adeudo.service';
import { AvisoInformativoAdeudoFotoService } from '../../../shared/services/aviso-informativo-adeudo-foto.service';
import Swal from 'sweetalert2';

export interface AvisoInformativoAdeudoEntregaDialogData {
  aviso: AvisoInformativoAdeudoModel;
  // true cuando se reabre este mismo dialog SOLO para consultar (y en su
  // caso agregar/quitar) las fotos de respaldo de una entrega YA
  // registrada -- mismo patrón que AvisoAdeudoEntregaDialogComponent.
  soloVerFotos?: boolean;
}

// Captura la sección "RAZÓN DE NOTIFICACIÓN" del Aviso Informativo de
// Adeudo -- mismo shape/opciones que AvisoBombaEntregaDialogComponent
// (reutiliza TIPOS_ENTREGA, sin ABONO -- este módulo no maneja cobro).
@Component({
  selector: 'app-aviso-informativo-adeudo-entrega-dialog',
  templateUrl: './aviso-informativo-adeudo-entrega-dialog.component.html',
  styleUrls: ['./aviso-informativo-adeudo-entrega-dialog.component.css']
})
export class AvisoInformativoAdeudoEntregaDialogComponent implements OnInit {

  tiposEntrega = TIPOS_ENTREGA;

  tipoEntrega = '';
  fechaEntrega = new Date().toISOString().substring(0, 10);
  nombreReceptor = '';
  parentescoReceptor = '';
  nombreNotificador = '';
  nombreTestigo1 = '';
  nombreTestigo2 = '';
  comentarioEntrega = '';

  guardando = false;
  error = '';

  fotos: AvisoInformativoAdeudoFotoModel[] = [];
  fotoUrls: { [fotoId: number]: string } = {};
  subiendoFoto = false;

  get requiereFotos(): boolean {
    return this.tipoEntrega === 'NO_ENCONTRADO' || this.tipoEntrega === 'SE_NEGO' || this.data.soloVerFotos === true;
  }

  get requiereReceptor(): boolean {
    return this.tipoEntrega === 'OTRA_PERSONA';
  }

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
    public dialogRef: MatDialogRef<AvisoInformativoAdeudoEntregaDialogComponent>,
    private readonly avisoInformativoAdeudoService: AvisoInformativoAdeudoService,
    private readonly avisoInformativoAdeudoFotoService: AvisoInformativoAdeudoFotoService,
    @Inject(MAT_DIALOG_DATA) public data: AvisoInformativoAdeudoEntregaDialogData
  ) { }

  ngOnInit(): void {
    this.cargarFotos();
  }

  private cargarFotos(): void {
    this.avisoInformativoAdeudoFotoService.listarPorAviso(this.data.aviso.avisoInformativoAdeudoId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00') return;
        this.fotos = resp.data || [];
        this.fotos.forEach(f => {
          if (this.fotoUrls[f.fotoId]) return;
          this.avisoInformativoAdeudoFotoService.getArchivoBlob(f.fotoId).subscribe({
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
    this.avisoInformativoAdeudoFotoService.subirFoto(this.data.aviso.avisoInformativoAdeudoId, archivo).subscribe({
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

  eliminarFoto(foto: AvisoInformativoAdeudoFotoModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Eliminar foto',
      text: '¿Confirmas eliminar esta foto de respaldo?',
      showCancelButton: true,
      confirmButtonText: 'Eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoInformativoAdeudoFotoService.eliminarFoto(foto.fotoId).subscribe({
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

    const datos: AvisoInformativoAdeudoEntregaModel = {
      tipoEntrega: this.tipoEntrega,
      nombreReceptor: this.requiereReceptor ? this.nombreReceptor.trim() : undefined,
      parentescoReceptor: this.requiereReceptor ? this.parentescoReceptor.trim() : undefined,
      nombreNotificador: this.nombreNotificador.trim(),
      nombreTestigo1: this.nombreTestigo1.trim() || undefined,
      nombreTestigo2: this.nombreTestigo2.trim() || undefined,
      comentarioEntrega: this.comentarioEntrega.trim() || undefined,
      // "naive" (sin Z ni offset) a propósito -- mismo criterio que en los
      // demás módulos de carta, para que no se corra un día al mostrarla.
      fechaEntrega: this.fechaEntrega ? this.fechaEntrega + 'T00:00:00' : undefined
    };

    this.avisoInformativoAdeudoService.marcarEntregada(this.data.aviso.avisoInformativoAdeudoId, datos).subscribe({
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
