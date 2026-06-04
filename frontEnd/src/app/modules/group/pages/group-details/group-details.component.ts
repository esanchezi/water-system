import { formatDate, CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { WaterGroupModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { GroupService } from 'src/app/modules/shared/services/group.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.css'],
})
export class GroupDetailsComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly receiptService = inject(ReceiptService);
  private readonly houseService= inject(HouseService);

  constructor(private currencyPipe: CurrencyPipe) {}

  colHeaders: Record<string, string> = {
    noFolio: 'N° Folio',
    fechaPago: 'Fecha',
    conceptoReceipt: 'Concepto',
    total: 'Total',
    observaciones: 'Observaciones',
    concepto: 'Concepto',
    montoRecibido: 'Monto Recibido',
    montoAplicado: 'Monto Aplicado',
    anio: 'Año'
  };

  displayColumns = Object.keys(this.colHeaders);

  waterGroup!: WaterGroupModel;
  listWaterUser: (WaterUserModel & { dataSource: MatTableDataSource<any>, paginatorRef?: any })[] = [];

    ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterGroup = JSON.parse(params['element']);
      }
      //this.configurarMapa();
     this.inicializarUsuarios();
    });
  }

  private inicializarUsuarios(): void {
    this.listWaterUser = (this.waterGroup?.listWaterUser || []).map(
      (user: WaterUserModel) => ({
        ...user,
        dataSource: new MatTableDataSource<WaterUserModel>([])
      })
    );
    }

  getReceipt(user: any): void {
    this.receiptService.getReceiptByNoUser(user.noUsuario)
      .subscribe({
        next: resp => this.processReceiptResponse(resp, user)
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
              fechaPago: pago.fechaPago ? formatDate(pago.fechaPago, 'dd/MM/yyyy', 'es-MX'): '',
              montoRecibido: this.currencyPipe.transform(
                pago.montoRecibido, 'MXN', 'symbol', '1.2-2'
              ),
              montoAplicado: this.currencyPipe.transform(
                pago.montoAplicado, 'MXN', 'symbol', '1.2-2'
              ),
              total: this.currencyPipe.transform(
                recibo.total, 'MXN', 'symbol', '1.2-2'
              ),
              noFolio: recibo.noFolio,
              conceptoReceipt: recibo.concepto,
              observaciones: recibo.observaciones,
              waterUser: recibo.waterUser
            });
          });
        } else {
          flatData.push({
            noFolio: recibo.noFolio,
            conceptoReceipt: recibo.concepto,
            observaciones: recibo.observaciones,
            total: recibo.total,
            waterUser: recibo.waterUser,
            concepto: null,
            montoAplicado: null,
            anio: null
          });
        }
      });
  
      user.dataSource = new MatTableDataSource<any>(flatData);
      if (user.paginatorRef) {
        user.dataSource.paginator = user.paginatorRef;
      }
    }
  
    trackByUser(index: number, user: WaterUserModel): number {
      return user.aguaUsuarioId; // o el id único que tengas
    }

}
