import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { formatDate, CurrencyPipe } from '@angular/common';
import { FormControl } from '@angular/forms';
import { WaterHouseModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { MatTableDataSource } from '@angular/material/table';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
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
  private readonly receiptService = inject(ReceiptService);
  private readonly houseService   = inject(HouseService);
  private readonly userService    = inject(UserService);

  constructor(private currencyPipe: CurrencyPipe) {}

  waterHouse!: WaterHouseModel;
  listWaterUser: (WaterUserModel & { dataSource: MatTableDataSource<any>, paginatorRef?: any })[] = [];

  colHeaders: Record<string, string> = {
    noFolio:         'N° Folio',
    fechaPago:       'Fecha',
    conceptoReceipt: 'Concepto',
    total:           'Total',
    observaciones:   'Observaciones',
    concepto:        'Concepto',
    montoRecibido:   'Monto Recibido',
    montoAplicado:   'Monto Aplicado',
    anio:            'Año'
  };

  displayColumns = Object.keys(this.colHeaders);

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

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterHouse = JSON.parse(params['element']);
      }
      this.configurarMapa();
      this.inicializarUsuarios();
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
          this.listWaterUser = [
            ...this.listWaterUser,
            { ...added, dataSource: new MatTableDataSource<any>([]) }
          ];
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
    this.listWaterUser = (this.waterHouse?.listWaterUser || []).map(
      (user: WaterUserModel) => ({
        ...user,
        dataSource: new MatTableDataSource<WaterUserModel>([])
      })
    );
  }

  getReceipt(user: any): void {
    this.receiptService.getReceiptByNoUser(user.noUsuario).subscribe({
      next: (resp: any) => this.processReceiptResponse(resp, user),
      error: (e: any) => console.error('Error al obtener recibos', e)
    });
  }

  private processReceiptResponse(resp: any, user: any): void {
    if (resp.metadata[0].code !== '00') return;

    const flatData: any[] = [];
    const listUser = resp.data as WaterReceiptModel[];

    listUser.forEach(recibo => {
      if (recibo.waterReceiptPayment?.length) {
        recibo.waterReceiptPayment.forEach(pago => {
          flatData.push({
            ...pago,
            fechaPago:       pago.fechaPago ? formatDate(pago.fechaPago, 'dd/MM/yyyy', 'es-MX') : '',
            montoRecibido:   this.currencyPipe.transform(pago.montoRecibido, 'MXN', 'symbol', '1.2-2'),
            montoAplicado:   this.currencyPipe.transform(pago.montoAplicado, 'MXN', 'symbol', '1.2-2'),
            total:           this.currencyPipe.transform(recibo.total, 'MXN', 'symbol', '1.2-2'),
            noFolio:         recibo.noFolio,
            conceptoReceipt: recibo.concepto,
            observaciones:   recibo.observaciones,
            waterUser:       recibo.waterUser
          });
        });
      } else {
        flatData.push({
          noFolio:         recibo.noFolio,
          conceptoReceipt: recibo.concepto,
          observaciones:   recibo.observaciones,
          total:           recibo.total,
          waterUser:       recibo.waterUser,
          concepto:        null,
          montoAplicado:   null,
          anio:            null
        });
      }
    });

    user.dataSource = new MatTableDataSource<any>(flatData);
    if (user.paginatorRef) {
      user.dataSource.paginator = user.paginatorRef;
    }
  }

  trackByUser(index: number, user: WaterUserModel): number {
    return user.aguaUsuarioId;
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
