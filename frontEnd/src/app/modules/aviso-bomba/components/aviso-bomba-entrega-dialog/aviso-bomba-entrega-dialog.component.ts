import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoBombaEntregaModel, AvisoBombaModel } from '../../../shared/models/AvisoBomba.model';
import { AvisoBombaService } from '../../../shared/services/aviso-bomba.service';

export interface AvisoBombaEntregaDialogData {
  aviso: AvisoBombaModel;
}

// Captura la sección "RAZÓN DE NOTIFICACIÓN" de la carta física de Aviso
// por uso de bomba -- mismo shape/opciones que AvisoAdeudoEntregaDialogComponent
// (reutiliza TIPOS_ENTREGA), pero sin fotos de respaldo por ahora (no se
// pidieron para este tipo de aviso).
@Component({
  selector: 'app-aviso-bomba-entrega-dialog',
  templateUrl: './aviso-bomba-entrega-dialog.component.html',
  styleUrls: ['./aviso-bomba-entrega-dialog.component.css']
})
export class AvisoBombaEntregaDialogComponent {

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
    public dialogRef: MatDialogRef<AvisoBombaEntregaDialogComponent>,
    private readonly avisoBombaService: AvisoBombaService,
    @Inject(MAT_DIALOG_DATA) public data: AvisoBombaEntregaDialogData
  ) { }

  guardar(): void {
    if (!this.formularioValido) return;
    this.guardando = true;
    this.error = '';

    const datos: AvisoBombaEntregaModel = {
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

    this.avisoBombaService.marcarEntregada(this.data.aviso.avisoBombaId, datos).subscribe({
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
