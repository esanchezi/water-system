import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { WaterHouseModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { PreregistroUsuarioService } from 'src/app/modules/shared/services/preregistro-usuario.service';
import { PREREGISTRO_ESTATUS_PENDIENTE, PreregistroUsuarioModel } from 'src/app/modules/shared/models/PreregistroUsuario.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { MatDialog } from '@angular/material/dialog';
import { NewUserComponent } from '../../../user/components/new-user/new-user.component';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import Swal from 'sweetalert2';

interface UserSearchResult {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;
}

@Component({
  selector: 'app-house-details',
  templateUrl: './house-details.component.html',
  styleUrls: ['./house-details.component.css']
})
export class HouseDetailsComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly houseService   = inject(HouseService);
  private readonly userService    = inject(UserService);
  private readonly preregistroService = inject(PreregistroUsuarioService);
  private readonly catalogService = inject(CatalogService);
  private readonly fb             = inject(FormBuilder);
  private readonly dialog         = inject(MatDialog);

  waterHouse!: WaterHouseModel;
  listWaterUser: WaterUserModel[] = [];

  // Gente que vive en la casa pero aún no es un usuario de agua formal.
  listPreregistro: PreregistroUsuarioModel[] = [];
  readonly PREREGISTRO_PENDIENTE = PREREGISTRO_ESTATUS_PENDIENTE;
  preregistroForm: FormGroup = this.fb.group({
    nombre:          ['', Validators.required],
    telefono:        [''],
    observaciones:   [''],
    motivoPendiente: [''],
    // Negocio que nunca va a tener su propio usuario (ej. tiendita
    // atendida por el usuario del domicilio) pero que sí se cuenta en el
    // censo de negocios.
    esNegocio:       [false],
    giroNegocioId:   [null],
    motivoNoUsuarioId: [null],
    // Deuda aproximada -- estimación manual, no hay cargos reales todavía
    // porque esta persona no es usuario formal.
    deudaAportaciones:   [null],
    deudaMultasRecargos: [null],
    deudaObservaciones:  ['']
  });
  girosNegocio: CatalogOptionModel[] = [];
  motivosNoUsuario: CatalogOptionModel[] = [];

  readonly DEFAULT_COORDS: google.maps.LatLngLiteral = {
    lat: 21.04386,
    lng: -101.56864
  };

  center!: google.maps.LatLngLiteral;
  markerPosition!: google.maps.LatLngLiteral;
  zoom = 18;
  ubicacionModificada = false;

  // Agregar usuario existente a esta casa (por N° de usuario, con autocompletado por nombre)
  userSearchCtrl = new FormControl('');
  userSearchResults: UserSearchResult[] = [];

  // Si el censo de personas está habilitado para cada usuario de la casa
  // (según la clasificación de uso capturada en app-user-uso) -- se
  // actualiza en vivo con cada cambio, no depende de recargar la página.
  censoHabilitadoPorUsuario = new Map<number, boolean>();

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterHouse = JSON.parse(params['element']);
      }
      this.configurarMapa();
      this.inicializarUsuarios();
    });

    this.catalogService.getOptionsByClave('NEGOCIO').subscribe({
      next: (opts) => this.girosNegocio = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('MOTIVO_NOUSUARIO').subscribe({
      next: (opts) => this.motivosNoUsuario = opts,
      error: (e: any) => console.error(e)
    });

    this.userSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const value = typeof term === 'string' ? term.trim() : '';
        if (value.length < 2) return of([]);
        return this.userService.searchUsersByNumber(value);
      })
    ).subscribe({
      next: (results: UserSearchResult[]) => this.userSearchResults = results || [],
      error: () => this.userSearchResults = []
    });
  }

  // Texto mostrado en el input del autocomplete al seleccionar un usuario
  displayUser = (user: UserSearchResult): string => {
    return user?.nombreCompleto ? `N° ${user.noUsuario} - ${user.nombreCompleto}` : '';
  };

  onUserSelected(event: MatAutocompleteSelectedEvent): void {
    const user: UserSearchResult = event.option.value;
    if (!user?.aguaUsuarioId || !this.waterHouse?.casaId) return;

    Swal.fire({ title: 'Agregando usuario...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userService.assignHouse(user.aguaUsuarioId, this.waterHouse.casaId).subscribe({
      next: (resp: any) => {
        const added = resp?.data?.[0];
        if (added) {
          this.listWaterUser = [...this.listWaterUser, added];
        }
        this.limpiarBusquedaUsuario();
        Swal.fire({ icon: 'success', title: 'Usuario agregado', text: `Se asignó a ${user.nombreCompleto} a esta casa.`, confirmButtonText: 'Aceptar' });
      },
      error: (e: any) => {
        this.limpiarBusquedaUsuario();
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo agregar el usuario a la casa.', confirmButtonText: 'Cerrar' });
        console.error('Error al asignar casa', e);
      }
    });
  }

  // Limpia el campo de búsqueda de usuario y las opciones del autocomplete
  limpiarBusquedaUsuario(): void {
    this.userSearchCtrl.setValue('');
    this.userSearchResults = [];
  }

  // Permite reubicar el marcador de la casa haciendo click o arrastrando el pin
  onMapClick(event: google.maps.MapMouseEvent): void {
    if (!event.latLng) return;
    this.markerPosition = { lat: event.latLng.lat(), lng: event.latLng.lng() };
    this.ubicacionModificada = true;
  }

  onMarkerDragEnd(event: google.maps.MapMouseEvent): void {
    if (!event.latLng) return;
    this.markerPosition = { lat: event.latLng.lat(), lng: event.latLng.lng() };
    this.ubicacionModificada = true;
  }

  // Reubica el marcador usando el GPS del dispositivo (útil cuando la
  // ubicación guardada quedó mal capturada y hay que corregirla parado
  // frente a la casa, en vez de buscarla a mano en el mapa).
  obteniendoGps = false;

  onUsarUbicacionGps(): void {
    if (!navigator.geolocation) {
      Swal.fire({ icon: 'error', title: 'No disponible', text: 'Este dispositivo/navegador no soporta GPS.', confirmButtonText: 'Cerrar' });
      return;
    }
    this.obteniendoGps = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.obteniendoGps = false;
        this.markerPosition = { lat: position.coords.latitude, lng: position.coords.longitude };
        this.center = this.markerPosition;
        this.zoom = 19;
        this.ubicacionModificada = true;
      },
      (error) => {
        this.obteniendoGps = false;
        const mensaje = error.code === error.PERMISSION_DENIED
          ? 'Debes permitir el acceso a tu ubicación para usar esta opción.'
          : 'No se pudo obtener tu ubicación GPS. Intenta de nuevo.';
        Swal.fire({ icon: 'error', title: 'No se pudo obtener la ubicación', text: mensaje, confirmButtonText: 'Cerrar' });
      },
      { enableHighAccuracy: true, timeout: 15000 }
    );
  }

  guardarUbicacion(): void {
    if (!this.waterHouse) return;
    const data = {
      casaNo:        this.waterHouse.casaNo,
      nombre:        this.waterHouse.nombre,
      observaciones: this.waterHouse.observaciones,
      lado:          this.waterHouse.lado,
      calleId:       this.waterHouse.calleId,
      lat:           this.markerPosition.lat,
      lng:           this.markerPosition.lng
    };
    Swal.fire({ title: 'Guardando ubicación...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.houseService.updateWaterHouse(this.waterHouse.casaId, data).subscribe({
      next: () => {
        this.waterHouse.lat = this.markerPosition.lat;
        this.waterHouse.lng = this.markerPosition.lng;
        this.center = this.markerPosition;
        this.ubicacionModificada = false;
        Swal.fire({ icon: 'success', title: 'Ubicación actualizada', confirmButtonText: 'Aceptar' });
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar la ubicación.', confirmButtonText: 'Cerrar' })
    });
  }

  private configurarMapa(): void {
    const lat = this.waterHouse?.lat ?? this.DEFAULT_COORDS.lat;
    const lng = this.waterHouse?.lng ?? this.DEFAULT_COORDS.lng;
    this.center = { lat, lng };
    this.markerPosition = { lat, lng };
  }

  private inicializarUsuarios(): void {
    this.listWaterUser = this.waterHouse?.listWaterUser || [];
  }

  trackByUser(index: number, user: WaterUserModel): number {
    return user.aguaUsuarioId;
  }

  onUsoCambio(user: WaterUserModel, evt: { esUsoDomestico: boolean; esUsoNegocio: boolean }): void {
    this.censoHabilitadoPorUsuario.set(user.aguaUsuarioId, evt.esUsoDomestico);
  }

  censoHabilitado(user: WaterUserModel): boolean {
    return this.censoHabilitadoPorUsuario.get(user.aguaUsuarioId) ?? false;
  }

  // Quita al usuario de esta casa sin borrarlo -- útil cuando se agregó
  // por error o el usuario ya no vive/atiende este domicilio.
  onQuitarUsuario(user: WaterUserModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Quitar usuario de la casa',
      text: `¿Confirmas quitar a ${user.person?.nombre || 'este usuario'} de esta casa? El usuario no se borra, solo se desvincula.`,
      showCancelButton: true,
      confirmButtonText: 'Quitar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.userService.unassignHouse(user.aguaUsuarioId).subscribe({
        next: () => {
          this.listWaterUser = this.listWaterUser.filter(u => u.aguaUsuarioId !== user.aguaUsuarioId);
          Swal.fire({ icon: 'success', title: 'Usuario desvinculado', confirmButtonText: 'Aceptar' });
        },
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo quitar el usuario de la casa.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }

  getPreregistros(): void {
    if (!this.waterHouse?.casaId) return;
    this.preregistroService.getByCasaId(this.waterHouse.casaId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code === '00') {
          this.listPreregistro = resp.data || [];
        }
      },
      error: (e: any) => console.error('Error al cargar preregistros', e)
    });
  }

  onSavePreregistro(): void {
    if (this.preregistroForm.invalid || !this.waterHouse?.casaId) return;
    this.preregistroService.create(this.waterHouse.casaId, this.preregistroForm.value).subscribe({
      next: (resp: any) => {
        this.listPreregistro = resp.data || [];
        this.preregistroForm.reset();
      },
      error: (e: any) => {
        console.error(e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo agregar el preregistro.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  // Abre el formulario normal de "Nuevo usuario", prellenado con el nombre
  // capturado en el preregistro. Al guardar exitosamente, esta casa y el
  // preregistro quedan enlazados automáticamente al usuario nuevo -- no
  // hace falta anotar el N° de usuario a mano ni abrir el alta por
  // separado.
  onConvertirAUsuario(item: PreregistroUsuarioModel): void {
    const dialogRef = this.dialog.open(NewUserComponent, {
      width: '900px',
      data: {
        preregistroId: item.preregistroId,
        casaIdDestino: this.waterHouse.casaId,
        nombrePrellenado: item.nombre
      }
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === true) {
        Swal.fire({ icon: 'success', title: 'Usuario creado', text: 'Se enlazó a esta casa y el preregistro quedó marcado como convertido.', confirmButtonText: 'Aceptar' });
        this.getPreregistros();
      }
    });
  }

  onMarcarDescartado(item: PreregistroUsuarioModel): void {
    Swal.fire({
      title: 'Descartar preregistro',
      text: '¿Por qué ya no se va a convertir en usuario? (opcional)',
      input: 'text',
      inputLabel: 'Motivo',
      showCancelButton: true,
      confirmButtonText: 'Descartar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.preregistroService.marcarDescartado(item.preregistroId, result.value).subscribe({
        next: () => this.getPreregistros(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo descartar el registro.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }

  save(updatedHouse: WaterHouseModel): void {
    this.houseService.updateWaterHouse(updatedHouse.casaId, updatedHouse).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Guardado correctamente',
          text: 'La información se actualizó.',
          confirmButtonText: 'Aceptar'
        });
      },
      error: (e: any) => console.error('Error al actualizar casa', e)
    });
  }
}
