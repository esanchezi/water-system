import { Component, Input, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';
import { WaterUserModel } from '../../models/WaterUser.model';
import { WaterUserCensusModel } from '../../models/WaterUserCensus.model';
import { WaterUserCensusService } from '../../services/water-user-census.service';

// Censo de personas de un usuario -- reutilizable donde haga falta ver o
// capturar el censo (ficha del usuario, ficha de la casa). Antes vivía
// solo dentro de details-user; se extrajo a componente aparte para no
// duplicar la lógica en cada lugar que lo necesite.
@Component({
  selector: 'app-user-censo',
  templateUrl: './user-censo.component.html',
  styleUrls: ['./user-censo.component.css']
})
export class UserCensoComponent implements OnInit {
  @Input() usuario!: WaterUserModel;

  // Opcional: si el padre ya tiene el formulario reactivo del usuario en
  // vivo (ej. details-user), puede pasar aquí el resultado calculado ahí
  // mismo (esUsoDomestico) para que el censo reaccione al toque, sin
  // depender de que "usuario" se haya vuelto a cargar tras guardar (el
  // objeto usuario puede venir de un route param que no se refresca solo).
  // Si no se pasa, se calcula igual que antes a partir de "usuario".
  @Input() censoHabilitado?: boolean;

  private readonly fb = inject(FormBuilder);
  private readonly censusService = inject(WaterUserCensusService);

  displayColumnsCenso: string[] = ['edadActual', 'observaciones', 'acciones'];
  dataSourceCenso = new MatTableDataSource<WaterUserCensusModel>();

  // La edad es opcional a propósito -- si no se da, la persona igual
  // cuenta en el censo, solo que aparece como "sin clasificar".
  censusForm: FormGroup = this.fb.group({
    edad: [''],
    observaciones: [''],
    cantidadSinEdad: [1]
  });

  ngOnInit(): void {
    this.getCenso();
  }

  // Mismo árbol que en la ficha de usuario (ver details-user.component.ts):
  // toma sin conexión -> nada; si no, aplica censo cuando habita domicilio,
  // o cuando es renta y no se marcó como negocio. Si ni habita ni es
  // renta, o si es negocio, no aplica (se oculta el formulario de alta;
  // la tabla y edición de lo ya capturado se dejan visibles por si hay
  // que corregir datos viejos).
  get censoAplica(): boolean {
    if (this.censoHabilitado !== undefined) return this.censoHabilitado;
    const u = this.usuario;
    if (!u) return false;
    const estatus = (u.estatusToma?.nombre || '').trim().toLowerCase();
    if (estatus === 'sin conexión' || estatus === 'sin conexion') return false;
    if (u.habitaDomicilio === true) return true;
    if (u.inmuebleRenta === true) return u.esNegocio !== true;
    return false;
  }

  getCenso(): void {
    if (!this.usuario?.aguaUsuarioId) return;
    this.censusService.getByAguaUsuarioId(this.usuario.aguaUsuarioId).subscribe({
      next: (resp: any) => this.processCensoResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processCensoResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataCenso: WaterUserCensusModel[] = resp.data;
    this.dataSourceCenso = new MatTableDataSource<WaterUserCensusModel>(dataCenso);
  }

  onSaveCenso(): void {
    const form = this.censusForm.value;
    const data = {
      edad: form.edad !== '' && form.edad !== null ? Number(form.edad) : null,
      observaciones: form.observaciones
    };
    this.censusService.create(this.usuario.aguaUsuarioId, data).subscribe({
      next: () => {
        this.censusForm.patchValue({ edad: '', observaciones: '' });
        this.getCenso();
      },
      error: (e: any) => this.mostrarError(e, 'No se pudo agregar la persona al censo.')
    });
  }

  // Agrega de golpe N personas "sin clasificar" (edad null) -- útil cuando
  // la familia no quiere dar edades individuales pero sí sabemos cuántas
  // son en total. Cada una editable después si algún día se obtiene el dato.
  onAgregarVariasSinEdad(): void {
    const cantidad = Number(this.censusForm.value.cantidadSinEdad);
    if (!cantidad || cantidad < 1) return;

    const llamadas = Array.from({ length: cantidad }, () =>
      this.censusService.create(this.usuario.aguaUsuarioId, { edad: null, observaciones: '' })
    );

    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    forkJoin(llamadas).subscribe({
      next: () => {
        this.censusForm.patchValue({ cantidadSinEdad: 1 });
        this.getCenso();
        Swal.fire({ icon: 'success', title: `${cantidad} persona(s) agregada(s) sin edad`, confirmButtonText: 'Aceptar' });
      },
      error: (e: any) => this.mostrarError(e, 'No se pudieron agregar las personas.')
    });
  }

  // Permite corregir después la edad (o quitarla) y las observaciones de
  // una persona ya capturada en el censo.
  onEditarCenso(item: WaterUserCensusModel): void {
    Swal.fire({
      title: 'Editar persona del censo',
      html:
        `<input id="swal-edad" type="number" min="0" class="swal2-input" placeholder="Edad (opcional)" value="${item.edad ?? ''}">` +
        `<input id="swal-observaciones" type="text" class="swal2-input" placeholder="Observaciones" value="${item.observaciones ?? ''}">`,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Guardar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const edadInput = (document.getElementById('swal-edad') as HTMLInputElement)?.value;
        const observaciones = (document.getElementById('swal-observaciones') as HTMLInputElement)?.value || '';
        return {
          edad: edadInput !== '' ? Number(edadInput) : null,
          observaciones
        };
      }
    }).then((result) => {
      if (!result.isConfirmed || !result.value) return;
      this.censusService.update(item.censoId, result.value).subscribe({
        next: () => this.getCenso(),
        error: (e: any) => this.mostrarError(e, 'No se pudo actualizar el registro.')
      });
    });
  }

  onDeactivateCenso(item: WaterUserCensusModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Quitar del censo',
      text: '¿Confirmas quitar a esta persona del censo (ej. ya no vive en el domicilio)?',
      showCancelButton: true,
      confirmButtonText: 'Quitar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.censusService.deactivate(item.censoId).subscribe({
        next: () => this.getCenso(),
        error: (e: any) => this.mostrarError(e, 'No se pudo quitar el registro.')
      });
    });
  }

  private mostrarError(e: any, fallback: string): void {
    console.error(e);
    const msg = e?.error?.metadata?.[0]?.description || fallback;
    Swal.fire({ icon: 'error', title: 'Error', text: msg, confirmButtonText: 'Cerrar' });
  }
}
