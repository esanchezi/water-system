import { Component, Input, inject, OnInit, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';
import { map } from 'rxjs/operators'; 

@Component({
  selector: 'app-house-form',
  templateUrl: './house-form.component.html',
  styleUrls: ['./house-form.component.css']
})
export class HouseFormComponent implements OnInit {

  @Input() waterHouse!: WaterHouseModel;
  @Output() formSubmit = new EventEmitter<WaterHouseModel>();

  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);

  form!: FormGroup;

  calles$ = this.catalogService.getCatalogByIdResponse(15).pipe(
    map(resp => resp.data[0].options)
  );

  readonly lados = [
    { value: 'D', label: 'Derecho' },
    { value: 'I', label: 'Izquierdo' }
  ];

  ngOnInit(): void {
    this.form = this.fb.group({
      casaId: [this.waterHouse?.casaId, Validators.required],
      calleId: [this.waterHouse?.calleId],
      casaNo: [this.waterHouse?.casaNo, Validators.required],
      nombre: [this.waterHouse?.nombre],
      lado: [this.waterHouse?.lado],
      observaciones: [this.waterHouse?.observaciones]
    });
  }
  save(): void {
    if (this.form.invalid) return;
    this.formSubmit.emit(this.form.value);
  }
}
