import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AvisoAdeudoAtencionModel, AvisoAdeudoModel, RESULTADOS_ATENCION } from '../../models/AvisoAdeudo.model';

export interface AvisoAdeudoAtencionDialogData {
  // Uno o varios avisos -- cuando se marca desde la alerta de la ficha del
  // usuario pueden venir varios pendientes a la vez (mismo pago los
  // resuelve todos); desde el historial de Cartas de adeudo siempre viene
  // uno solo. El resultado/comentario/folio capturados se aplican igual a
  // todos los que vengan en la lista.
  avisos: AvisoAdeudoModel[];
}

// Captura cómo se resolvió el seguimiento de una (o varias) cartas de
// adeudo ya entregadas: categoría fija (para poder contar/filtrar cuántas
// terminaron pagadas vs condonadas), comentario libre opcional para el
// detalle del caso, y -- opcional -- el folio del recibo real que
// corresponde al pago, que el backend valida contra agua_recibo antes de
// guardarlo como referencia. El monto cobrado NO se vuelve a capturar
// aquí: ya vive en el recibo (Detalle de pagos → Agregar recibo).
@Component({
  selector: 'app-aviso-adeudo-atencion-dialog',
  templateUrl: './aviso-adeudo-atencion-dialog.component.html',
  styleUrls: ['./aviso-adeudo-atencion-dialog.component.css']
})
export class AvisoAdeudoAtencionDialogComponent {

  resultados = RESULTADOS_ATENCION;

  resultadoAtencion = '';
  comentarioAtencion = '';
  folioReciboVinculado: number | null = null;
  // Independiente del recibo -- se puede ligar un convenio y un recibo a
  // la vez (ej. el convenio condona parte de la deuda y hay un recibo por
  // el resto). Ver AvisoAdeudoAtencionRequestDto en el backend.
  folioConvenioVinculado: number | null = null;
  // Solo cuando resultadoAtencion = CONVENIO -- fecha en la que el usuario
  // se comprometió a pagar. Mientras no venza, se pausa el Segundo aviso
  // para este usuario (ver AvisoAdeudoService.enriquecerConUltimoAviso()
  // en el backend). Si incumple (pasa la fecha sin pagar), vuelve a
  // requerirse el Segundo aviso automáticamente; si cumple, un adeudo
  // futuro se trata como uno nuevo (Primer aviso otra vez).
  fechaCompromiso = '';

  get esConvenio(): boolean {
    return this.resultadoAtencion === 'CONVENIO';
  }

  get requiereFolioOpcionalHint(): boolean {
    return this.resultadoAtencion === 'PAGADO';
  }

  get formularioValido(): boolean {
    if (!this.resultadoAtencion) return false;
    if (this.esConvenio && !this.fechaCompromiso) return false;
    return true;
  }

  // Se arma en el componente y no en el template -- los templates de
  // Angular no soportan arrow functions dentro de una interpolación.
  get foliosTexto(): string {
    return this.data.avisos.map(a => a.folioNotificacion).join(', ');
  }

  constructor(
    public dialogRef: MatDialogRef<AvisoAdeudoAtencionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AvisoAdeudoAtencionDialogData
  ) { }

  guardar(): void {
    if (!this.formularioValido) return;
    const datos: AvisoAdeudoAtencionModel = {
      resultadoAtencion: this.resultadoAtencion,
      comentarioAtencion: this.comentarioAtencion.trim() || undefined,
      folioReciboVinculado: this.folioReciboVinculado || undefined,
      folioConvenioVinculado: this.folioConvenioVinculado || undefined,
      // Se manda tal cual ("yyyy-MM-dd", sin Z ni offset, sin pasar por
      // Date/toISOString) -- el backend la recibe como LocalDate puro, sin
      // ningún concepto de zona horaria de por medio, así que no hay
      // riesgo de que se corra un día al guardarla o mostrarla de vuelta.
      fechaCompromiso: this.esConvenio && this.fechaCompromiso ? this.fechaCompromiso : undefined
    };
    this.dialogRef.close(datos);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
