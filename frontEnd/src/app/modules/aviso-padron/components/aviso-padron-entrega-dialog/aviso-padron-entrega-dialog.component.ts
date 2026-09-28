import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoPadronEntregaModel, AvisoPadronModel } from '../../../shared/models/AvisoPadron.model';
import { AvisoPadronService } from '../../../shared/services/aviso-padron.service';

export interface AvisoPadronEntregaDialogData {
  aviso: AvisoPadronModel;
}

// Captura la sección "RAZÓN DE NOTIFICACIÓN" de la carta física de Aviso de
// actualización de padrón -- mismo shape/opciones que
// AvisoAdeudoEntregaDialogComponent/AvisoBombaEntregaDialogComponent
// (reutiliza TIPOS_ENTREGA), sin fotos de respaldo (mismo alcance que Aviso
// de bomba).
@Component({
  selector: 'app-aviso-padron-entrega-dialog',
  templateUrl: './aviso-padron-entrega-dialog.component.html',
  styleUrls: ['./aviso-padron-entrega-dialog.component.css']
})
export class AvisoPadronEntregaDialogComponent {

  tiposEntrega = TIPOS_ENTREGA;

  tipoEntrega = '';
  fechaEntrega = new Date().toISOString().substring(0, 10);
  nombreReceptor = '';
  parentescoReceptor = '';
  nombreNotificador = '';
  nombreTestigo1 = '';
  nombreTestigo2 = '';

  guardando = false;
  error = '';

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
    public dialogRef: MatDialogRef<AvisoPadronEntregaDialogComponent>,
    private readonly avisoPadronService: AvisoPadronService,
    @Inject(MAT_DIALOG_DATA) public data: AvisoPadronEntregaDialogData
  ) { }

  guardar(): void {
    if (!this.formularioValido) return;
    this.guardando = true;
    this.error = '';

    const datos: AvisoPadronEntregaModel = {
      tipoEntrega: this.tipoEntrega,
      nombreReceptor: this.requiereReceptor ? this.nombreReceptor.trim() : undefined,
      parentescoReceptor: this.requiereReceptor ? this.parentescoReceptor.trim() : undefined,
      nombreNotificador: this.nombreNotificador.trim(),
      nombreTestigo1: this.nombreTestigo1.trim() || undefined,
      nombreTestigo2: this.nombreTestigo2.trim() || undefined,
      // Se manda "naive" (sin Z ni offset) a propósito -- mismo criterio
      // que AvisoAdeudoEntregaDialogComponent, para que no se corra un día
      // al guardarla/mostrarla de vuelta.
      fechaEntrega: this.fechaEntrega ? this.fechaEntrega + 'T00:00:00' : undefined
    };

    this.avisoPadronService.marcarEntregada(this.data.aviso.avisoPadronId, datos).subscribe({
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
