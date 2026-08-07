import { Component, Input, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import Swal from 'sweetalert2';

import { CatalogOptionModel } from '../../models/Catalog.model';
import { WaterUserChargeModel } from '../../models/WaterUserCharge.model';
import { WaterUserModel } from '../../models/WaterUser.model';
import { CatalogService } from '../../services/catalog.service';
import { UserChargeService } from '../../services/user-charge.service';

// Cargos / multas de un usuario -- extraído de details-user para poder
// reutilizarlo también en la ficha de casa (house-details), igual que ya
// se hizo con UserReceiptComponent, UserCensoComponent y UserUsoComponent.
@Component({
  selector: 'app-user-charges',
  templateUrl: './user-charges.component.html',
  styleUrls: ['./user-charges.component.css']
})
export class UserChargesComponent implements OnInit {
  @Input() usuario!: WaterUserModel;

  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly userChargeService = inject(UserChargeService);

  chargeForm: FormGroup = this.fb.group({});
  paymentForm: FormGroup = this.fb.group({});

  displayColumnsCharge: string[] = ['concepto', 'descripcion', 'monto', 'fechaStr', 'montoPagado', 'montoCondonado', 'saldo', 'estatusPago'];
  dataSourceCharge = new MatTableDataSource<WaterUserChargeModel>();

  conceptosCargo: CatalogOptionModel[] = [];

  totalMonto = 0;
  totalPagado = 0;
  totalCondonado = 0;
  totalSaldo = 0;

  @ViewChild(MatPaginator) paginatorCharge!: MatPaginator;

  ngOnInit(): void {
    this.chargeForm = this.fb.group({
      conceptoId: ['', Validators.required],
      monto: ['', Validators.required],
      fecha: ['', Validators.required],
      descripcion: [''],
      comentario: ['']
    });
    this.paymentForm = this.fb.group({
      aguaUsuarioCargoId: ['', Validators.required],
      noFolio: ['', Validators.required],
      montoAplicado: ['', Validators.required]
    });

    this.catalogService.getOptionsByClave('CONCEPTO_CARGO_EXTRA').subscribe({
      next: (opts) => this.conceptosCargo = opts,
      error: (e: any) => console.error(e)
    });

    this.getCharges();
  }

  getCharges(): void {
    if (!this.usuario?.noUsuario) return;
    this.userChargeService.getChargesByUser(this.usuario.noUsuario).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code === '00') {
          const dataCharge: WaterUserChargeModel[] = resp.data || [];
          this.dataSourceCharge = new MatTableDataSource<WaterUserChargeModel>(dataCharge);
          this.dataSourceCharge.paginator = this.paginatorCharge;
          this.calculateChargeTotals(dataCharge);
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  private calculateChargeTotals(dataCharge: WaterUserChargeModel[]): void {
    this.totalMonto = dataCharge.reduce((acc, c) => acc + (Number(c.monto) || 0), 0);
    this.totalPagado = dataCharge.reduce((acc, c) => acc + (Number(c.montoPagado) || 0), 0);
    this.totalCondonado = dataCharge.reduce((acc, c) => acc + (Number(c.montoCondonado) || 0), 0);
    this.totalSaldo = dataCharge.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);
  }

  onSaveCharge(): void {
    if (this.chargeForm.invalid || !this.usuario?.noUsuario) return;
    const form = this.chargeForm.value;
    const data = {
      noUsuario: this.usuario.noUsuario,
      conceptoId: form.conceptoId,
      descripcion: form.descripcion,
      monto: form.monto,
      fecha: form.fecha,
      comentario: form.comentario
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userChargeService.saveCharge(data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Cargo registrado', confirmButtonText: 'Aceptar' });
        this.chargeForm.reset();
        this.getCharges();
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar el cargo.', confirmButtonText: 'Cerrar' })
    });
  }

  // Un cargo puede saldarse en uno o varios abonos, cada uno con su propio
  // folio de recibo. Esto se puede llamar varias veces hasta que saldo=0.
  onAddPayment(): void {
    if (this.paymentForm.invalid) return;
    const form = this.paymentForm.value;
    const data = {
      noFolio: form.noFolio,
      montoAplicado: form.montoAplicado
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userChargeService.addPayment(form.aguaUsuarioCargoId, data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Abono registrado', confirmButtonText: 'Aceptar' });
        this.paymentForm.reset();
        this.getCharges();
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al registrar el abono. Verifica que el folio exista.', confirmButtonText: 'Cerrar' })
    });
  }
}
