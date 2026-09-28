import { Component, OnInit, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { ReciboRevisionService } from '../../../shared/services/recibo-revision.service';
import {
  ReciboRevisionFotoModel,
  ReciboRevisionModel,
  ResultadoRevisionInfo,
  infoResultado
} from '../../../shared/models/RevisionRecibo.model';

// Formulario de captura de un recibo dentro de una foto -- un registro
// por foto, se limpia después de cada "Comparar" para poder seguir
// capturando el siguiente recibo de la misma imagen.
interface FormRevision {
  noFolioCapturado: number | null;
  noUsuarioCapturado: number | null;
  montoTexto: string;
  observaciones: string;
}

@Component({
  selector: 'app-recibo-revision-list',
  templateUrl: './recibo-revision-list.component.html',
  styleUrls: ['./recibo-revision-list.component.css']
})
export class ReciboRevisionListComponent implements OnInit {

  private readonly reciboRevisionService = inject(ReciboRevisionService);
  private readonly snackBar = inject(MatSnackBar);

  cargando = false;
  subiendoFoto = false;
  observacionesFoto = '';

  fotos: ReciboRevisionFotoModel[] = [];

  // Cacheadas por fotoId, igual que en water-valves.component.ts -- no
  // son getters que arman objetos nuevos en cada ciclo (eso ya causó un
  // ciclo infinito de detección de cambios antes), solo diccionarios que
  // se llenan una vez y se leen directo en el template.
  fotoUrls: { [fotoId: number]: string } = {};
  formNuevaRevision: { [fotoId: number]: FormRevision } = {};
  guardandoRevision: { [fotoId: number]: boolean } = {};

  ngOnInit(): void {
    this.cargarFotos();
  }

  private cargarFotos(): void {
    this.cargando = true;
    this.reciboRevisionService.listarFotos().subscribe({
      next: (resp: any) => {
        this.fotos = resp?.data ?? [];
        this.cargando = false;
        this.cargarFotoUrls();
        this.inicializarFormularios();
      },
      error: (e) => {
        console.error(e);
        this.cargando = false;
        this.openSnackBar('No se pudieron cargar las fotos', 'Error');
      }
    });
  }

  private cargarFotoUrls(): void {
    for (const foto of this.fotos) {
      if (!foto.fotoId || this.fotoUrls[foto.fotoId]) {
        continue;
      }
      const fotoId = foto.fotoId;
      this.reciboRevisionService.getArchivoBlob(fotoId).subscribe({
        next: (blob) => {
          this.fotoUrls[fotoId] = URL.createObjectURL(blob);
        },
        error: (e) => console.error('No se pudo cargar la foto', fotoId, e)
      });
    }
  }

  private inicializarFormularios(): void {
    for (const foto of this.fotos) {
      if (!foto.fotoId || this.formNuevaRevision[foto.fotoId]) {
        continue;
      }
      this.formNuevaRevision[foto.fotoId] = this.formVacio();
    }
  }

  private formVacio(): FormRevision {
    return { noFolioCapturado: null, noUsuarioCapturado: null, montoTexto: '', observaciones: '' };
  }

  subirFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) {
      return;
    }
    this.subiendoFoto = true;
    this.reciboRevisionService.subirFoto(archivo, this.observacionesFoto).subscribe({
      next: (resp: any) => {
        this.subiendoFoto = false;
        input.value = '';
        if (resp?.metadata?.code === '00') {
          this.observacionesFoto = '';
          this.openSnackBar('Foto subida', 'Éxito');
          this.cargarFotos();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudo subir la foto', 'Error');
        }
      },
      error: (e) => {
        this.subiendoFoto = false;
        input.value = '';
        console.error(e);
        this.openSnackBar('No se pudo subir la foto', 'Error');
      }
    });
  }

  eliminarFoto(foto: ReciboRevisionFotoModel): void {
    if (!foto.fotoId) {
      return;
    }
    const fotoId = foto.fotoId;
    Swal.fire({
      title: '¿Eliminar esta foto?',
      text: 'También se eliminan las revisiones capturadas en ella.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.reciboRevisionService.eliminarFoto(fotoId).subscribe({
        next: () => {
          if (this.fotoUrls[fotoId]) {
            URL.revokeObjectURL(this.fotoUrls[fotoId]);
            delete this.fotoUrls[fotoId];
          }
          delete this.formNuevaRevision[fotoId];
          this.openSnackBar('Foto eliminada', 'Éxito');
          this.cargarFotos();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo eliminar la foto', 'Error');
        }
      });
    });
  }

  agregarRevision(foto: ReciboRevisionFotoModel): void {
    if (!foto.fotoId) {
      return;
    }
    const fotoId = foto.fotoId;
    const form = this.formNuevaRevision[fotoId];
    if (!form?.noFolioCapturado) {
      this.openSnackBar('Captura el número de folio', 'Atención');
      return;
    }
    this.guardandoRevision[fotoId] = true;
    this.reciboRevisionService.agregarRevision(fotoId, {
      noFolioCapturado: form.noFolioCapturado,
      noUsuarioCapturado: form.noUsuarioCapturado,
      montoTexto: form.montoTexto?.trim() || undefined,
      observaciones: form.observaciones?.trim() || undefined
    }).subscribe({
      next: (resp: any) => {
        this.guardandoRevision[fotoId] = false;
        if (resp?.metadata?.code === '00') {
          const revision: ReciboRevisionModel = resp.data?.[0];
          if (!foto.revisiones) {
            foto.revisiones = [];
          }
          foto.revisiones.push(revision);
          this.formNuevaRevision[fotoId] = this.formVacio();
          this.avisarResultado(revision);
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudo registrar la revisión', 'Error');
        }
      },
      error: (e) => {
        this.guardandoRevision[fotoId] = false;
        console.error(e);
        this.openSnackBar('No se pudo registrar la revisión', 'Error');
      }
    });
  }

  private avisarResultado(revision: ReciboRevisionModel): void {
    if (revision.resultado === 'COINCIDE') {
      this.openSnackBar(`Folio ${revision.noFolioCapturado}: coincide con el sistema`, 'Éxito');
    } else if (revision.resultado === 'DISCREPANCIA') {
      this.openSnackBar(`Folio ${revision.noFolioCapturado}: el usuario no coincide con el sistema`, 'Atención');
    } else if (revision.resultado === 'NO_ENCONTRADO') {
      this.openSnackBar(`Folio ${revision.noFolioCapturado}: no existe en el sistema`, 'Atención');
    }
  }

  eliminarRevision(foto: ReciboRevisionFotoModel, revision: ReciboRevisionModel): void {
    if (!revision.revisionId) {
      return;
    }
    Swal.fire({
      title: '¿Eliminar esta revisión?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.reciboRevisionService.eliminarRevision(revision.revisionId!).subscribe({
        next: () => {
          if (foto.revisiones) {
            foto.revisiones = foto.revisiones.filter(r => r.revisionId !== revision.revisionId);
          }
          this.openSnackBar('Revisión eliminada', 'Éxito');
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo eliminar la revisión', 'Error');
        }
      });
    });
  }

  marcarResuelto(foto: ReciboRevisionFotoModel, revision: ReciboRevisionModel): void {
    if (!revision.revisionId) {
      return;
    }
    this.reciboRevisionService.actualizarRevision(revision.revisionId, { resultado: 'COINCIDE' }).subscribe({
      next: () => {
        revision.resultado = 'COINCIDE';
        this.openSnackBar('Marcada como resuelta', 'Éxito');
      },
      error: (e) => {
        console.error(e);
        this.openSnackBar('No se pudo actualizar', 'Error');
      }
    });
  }

  infoResultado(resultado: string | undefined): ResultadoRevisionInfo {
    return infoResultado(resultado);
  }

  resumenFoto(foto: ReciboRevisionFotoModel): string {
    const total = foto.revisiones?.length ?? 0;
    if (total === 0) {
      return 'Sin recibos capturados';
    }
    const discrepancias = foto.revisiones!.filter(r => r.resultado === 'DISCREPANCIA' || r.resultado === 'NO_ENCONTRADO').length;
    if (discrepancias > 0) {
      return `${total} recibo(s) -- ${discrepancias} con discrepancia`;
    }
    return `${total} recibo(s) -- todos coinciden`;
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 3500 });
  }
}
