import { Component, Input, OnChanges, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import Swal from 'sweetalert2';
import { WaterUserModel } from '../../models/WaterUser.model';
import {
  RESULTADOS_REVISION,
  TIPOS_REVISION,
  WaterUserRevisionFotoModel,
  WaterUserRevisionModel
} from '../../models/WaterUserRevision.model';
import { WaterUserRevisionService } from '../../services/water-user-revision.service';
import { WaterUserRevisionFotoService } from '../../services/water-user-revision-foto.service';

// Historial de revisiones de un usuario -- física (toma/medidor), de datos
// de padrón, o general -- con evidencia fotográfica opcional. Reutilizable
// como app-user-censo: se agrega dentro de la ficha del usuario, recibe el
// usuario por Input y resuelve todo internamente (cargar, registrar,
// subir/ver/eliminar fotos).
@Component({
  selector: 'app-user-revision',
  templateUrl: './user-revision.component.html',
  styleUrls: ['./user-revision.component.css']
})
export class UserRevisionComponent implements OnChanges {
  @Input() usuario!: WaterUserModel;

  private readonly fb = inject(FormBuilder);
  private readonly revisionService = inject(WaterUserRevisionService);
  private readonly fotoService = inject(WaterUserRevisionFotoService);

  tiposRevision = TIPOS_REVISION;
  resultadosRevision = RESULTADOS_REVISION;

  historial: WaterUserRevisionModel[] = [];
  cargando = false;

  // Fotos ya subidas por revisión, y las URLs locales para mostrarlas (el
  // endpoint del archivo pide JWT, no puede ser un [src] directo -- ver
  // WaterUserRevisionFotoService.getArchivoBlob).
  fotosPorRevision: { [revisionId: number]: WaterUserRevisionFotoModel[] } = {};
  fotoUrls: { [fotoId: number]: string } = {};
  subiendoFotoPara: number | null = null;

  revisionForm: FormGroup = this.fb.group({
    fecha: ['', Validators.required],
    tipo: ['GENERAL', Validators.required],
    resultado: ['BIEN', Validators.required],
    comentario: [''],
  });

  ngOnChanges(): void {
    if (this.usuario?.aguaUsuarioId) {
      this.cargarHistorial();
    }
  }

  private cargarHistorial(): void {
    this.cargando = true;
    this.revisionService.historialPorUsuario(this.usuario.aguaUsuarioId).subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.code !== '00') return;
        this.historial = resp.data || [];
        this.historial.forEach(r => this.cargarFotos(r.revisionId));
      },
      error: () => { this.cargando = false; }
    });
  }

  registrar(): void {
    if (this.revisionForm.invalid) {
      this.revisionForm.markAllAsTouched();
      return;
    }
    const valores = this.revisionForm.value;
    this.revisionService.registrar(this.usuario.aguaUsuarioId, {
      fechaStr: valores.fecha,
      tipo: valores.tipo,
      resultado: valores.resultado,
      comentario: valores.comentario,
    }).subscribe({
      next: () => {
        this.revisionForm.reset({ fecha: '', tipo: 'GENERAL', resultado: 'BIEN', comentario: '' });
        this.cargarHistorial();
      },
      error: (e: any) => {
        console.error(e);
        Swal.fire('Error', 'No se pudo registrar la revisión', 'error');
      }
    });
  }

  eliminar(revision: WaterUserRevisionModel): void {
    Swal.fire({
      icon: 'warning',
      title: '¿Eliminar esta revisión?',
      text: 'Se quitará del historial (no se puede deshacer desde aquí).',
      showCancelButton: true,
      confirmButtonText: 'Eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.revisionService.eliminar(revision.revisionId).subscribe({
        next: () => this.cargarHistorial(),
        error: (e: any) => { console.error(e); Swal.fire('Error', 'No se pudo eliminar la revisión', 'error'); }
      });
    });
  }

  tipoLabel(value: string): string {
    return this.tiposRevision.find(t => t.value === value)?.label || value;
  }

  resultadoLabel(value: string): string {
    return this.resultadosRevision.find(r => r.value === value)?.label || value;
  }

  // ----- Fotos de evidencia -----

  private cargarFotos(revisionId: number): void {
    this.fotoService.listarPorRevision(revisionId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00') return;
        const fotos: WaterUserRevisionFotoModel[] = resp.data || [];
        this.fotosPorRevision[revisionId] = fotos;
        fotos.forEach(f => {
          if (this.fotoUrls[f.fotoId]) return;
          this.fotoService.getArchivoBlob(f.fotoId).subscribe({
            next: (blob: Blob) => this.fotoUrls[f.fotoId] = URL.createObjectURL(blob),
            error: (e: any) => console.error('Error al cargar foto', e)
          });
        });
      },
      error: (e: any) => console.error('Error al consultar fotos de la revisión', e)
    });
  }

  subirFoto(revisionId: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    this.subiendoFotoPara = revisionId;
    this.fotoService.subirFoto(revisionId, archivo).subscribe({
      next: () => {
        this.subiendoFotoPara = null;
        input.value = '';
        this.cargarFotos(revisionId);
      },
      error: (e: any) => {
        this.subiendoFotoPara = null;
        input.value = '';
        console.error(e);
        Swal.fire('Error', 'No se pudo subir la foto', 'error');
      }
    });
  }

  eliminarFoto(revisionId: number, foto: WaterUserRevisionFotoModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Eliminar foto',
      text: '¿Seguro que quieres eliminar esta foto?',
      showCancelButton: true,
      confirmButtonText: 'Eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.fotoService.eliminarFoto(foto.fotoId).subscribe({
        next: () => {
          if (this.fotoUrls[foto.fotoId]) {
            URL.revokeObjectURL(this.fotoUrls[foto.fotoId]);
            delete this.fotoUrls[foto.fotoId];
          }
          this.cargarFotos(revisionId);
        },
        error: (e: any) => { console.error(e); Swal.fire('Error', 'No se pudo eliminar la foto', 'error'); }
      });
    });
  }
}
