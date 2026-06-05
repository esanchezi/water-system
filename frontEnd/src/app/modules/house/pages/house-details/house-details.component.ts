import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { formatDate, CurrencyPipe } from '@angular/common';
import { WaterHouseModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { MatTableDataSource } from '@angular/material/table';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-house-details',
  templateUrl: './house-details.component.html',
  styleUrls: ['./house-details.component.css']
})
export class HouseDetailsComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly receiptService = inject(ReceiptService);
  private readonly houseService   = inject(HouseService);

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

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterHouse = JSON.parse(params['element']);
      }
      this.configurarMapa();
      this.inicializarUsuarios();
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
