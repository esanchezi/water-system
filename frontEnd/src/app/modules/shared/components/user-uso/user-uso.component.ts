import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import Swal from 'sweetalert2';

import { CatalogOptionModel } from '../../models/Catalog.model';
import { WaterUserModel } from '../../models/WaterUser.model';
import { CatalogService } from '../../services/catalog.service';
import { UserService } from '../../services/user.service';

// Clasificación de "uso" de un usuario/toma -- extraído de details-user
// para poder reutilizarlo también en la ficha de casa (house-details),
// donde antes no existía forma de capturar esto sin salir a la ficha
// completa del usuario. Guarda con un endpoint chico (updateUso) que
// solo toca estos campos, sin arriesgar pisar noUsuario/alias/cuota/etc.
//
// Árbol de clasificación:
// 1. Estatus de la toma -- si "Sin conexión", no se pregunta nada más.
// 2/3. Si está conectada, se pregunta habita domicilio / es renta.
// 4. Si habita domicilio -> uso doméstico automático, no se pregunta negocio.
// 6. Si es renta -> sí se pregunta doméstico/negocio (puede ser cualquiera).
// 5/7. Doméstico -> habilita censo de personas (ver usoCambio, lo escucha el padre).
// 8. Negocio -> habilita "censo de negocio" (giro + tamaño, aquí mismo).
@Component({
  selector: 'app-user-uso',
  templateUrl: './user-uso.component.html',
  styleUrls: ['./user-uso.component.css']
})
export class UserUsoComponent implements OnInit {
  @Input() usuario!: WaterUserModel;

  // El padre (ej. house-details) escucha esto para decidir si habilita
  // el acordeón de Censo de personas para este mismo usuario.
  @Output() usoCambio = new EventEmitter<{ esUsoDomestico: boolean; esUsoNegocio: boolean }>();

  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly userService = inject(UserService);

  form: FormGroup = this.fb.group({});
  estatusToma: CatalogOptionModel[] = [];
  girosNegocio: CatalogOptionModel[] = [];

  ngOnInit(): void {
    this.form = this.fb.group({
      fkEstatusTomaId: [this.usuario?.estatusToma?.catalogoOpcionesId || null],
      tieneToma: [this.usuario?.tieneToma || false],
      habitaDomicilio: [this.usuario?.habitaDomicilio || false],
      inmuebleRenta: [this.usuario?.inmuebleRenta || false],
      // Sin default: null significa "todavía no se elige", para no
      // confundir "sin contestar" con "confirmado doméstico".
      esNegocio: [this.usuario?.esNegocio ?? null],
      giroNegocioId: [this.usuario?.giroNegocioId || null],
      tieneLocal: [this.usuario?.tieneLocal || false],
      localRentadoPorUsuario: [this.usuario?.localRentadoPorUsuario || false]
    });

    this.catalogService.getOptionsByClave('ESTATUS_TOMA').subscribe({
      next: (opts) => this.estatusToma = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('NEGOCIO').subscribe({
      next: (opts) => this.girosNegocio = opts,
      error: (e: any) => console.error(e)
    });

    this.emitUso();
    this.form.valueChanges.subscribe(() => this.emitUso());
  }

  get tomaSinConexion(): boolean {
    const id = Number(this.form.value.fkEstatusTomaId);
    if (!id) return false;
    const nombre = (this.estatusToma.find(e => e.catalogoOpcionesId === id)?.nombre || '').trim().toLowerCase();
    return nombre === 'sin conexión' || nombre === 'sin conexion';
  }

  // Elección doméstico/negocio mutuamente excluyente -- solo aplica en la
  // rama de renta (ahí sí es una cosa u otra, no ambas).
  get mostrarUsoToggle(): boolean {
    const f = this.form.value;
    return !!f.inmuebleRenta && !f.habitaDomicilio;
  }

  // Checkbox independiente de "es negocio" -- se muestra en cualquier caso
  // que NO sea la rama de renta-sin-habitar (ahí ya hay un radio
  // doméstico/negocio excluyente). Cubre: vive aquí y también tiene una
  // tiendita, Y el caso de toma conectada sin habitar y sin ser renta pero
  // que sí es un negocio (ej. bodega/local que el mismo dueño usa, sin
  // vivir ahí ni rentarlo a nadie).
  get mostrarNegocioIndependiente(): boolean {
    const f = this.form.value;
    if (this.tomaSinConexion) return false;
    return !(f.inmuebleRenta && !f.habitaDomicilio);
  }

  // esNegocio puede ser null ("todavía no se elige") en la rama de renta --
  // solo cuenta como doméstico o negocio cuando se elige explícitamente,
  // para no dar por hecho una respuesta que nadie confirmó.
  get esUsoDomestico(): boolean {
    const f = this.form.value;
    if (f.habitaDomicilio) return true;
    if (f.inmuebleRenta) return f.esNegocio === false;
    return false;
  }

  // Negocio: no depende de habitaDomicilio/inmuebleRenta -- basta con que
  // esté marcado explícitamente, ya sea junto con habitar el domicilio, en
  // la rama de renta (vía el radio), o solo, sin habitar ni ser renta.
  get esUsoNegocio(): boolean {
    return this.form.value.esNegocio === true;
  }

  private emitUso(): void {
    this.usoCambio.emit({ esUsoDomestico: this.esUsoDomestico, esUsoNegocio: this.esUsoNegocio });
  }

  onGuardar(): void {
    if (!this.usuario?.aguaUsuarioId) return;
    const f = this.form.value;
    const body = {
      estatusTomaId: f.fkEstatusTomaId || null,
      tieneToma: f.tieneToma,
      habitaDomicilio: f.habitaDomicilio,
      inmuebleRenta: f.inmuebleRenta,
      esNegocio: f.esNegocio,
      giroNegocioId: f.giroNegocioId || null,
      tieneLocal: f.tieneLocal,
      localRentadoPorUsuario: f.localRentadoPorUsuario
    };

    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userService.updateUso(this.usuario.aguaUsuarioId, body).subscribe({
      next: () => Swal.fire({ icon: 'success', title: 'Guardado correctamente', confirmButtonText: 'Aceptar' }),
      error: (e: any) => {
        console.error(e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar.', confirmButtonText: 'Cerrar' });
      }
    });
  }
}
