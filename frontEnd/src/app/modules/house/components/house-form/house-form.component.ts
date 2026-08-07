import { Component, Input, inject, OnInit, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';

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

  // Misma cascada Sección -> Calle que en house-new: se carga el catálogo
  // completo una sola vez y se filtra en el cliente según la Sección
  // elegida (cada calle puede tener una sección asignada desde Catálogos).
  secciones: CatalogOptionModel[] = [];
  private todasLasCalles: CatalogOptionModel[] = [];
  callesDeSeccion: CatalogOptionModel[] = [];

  readonly lados = [
    { value: 'D', label: 'Derecho' },
    { value: 'I', label: 'Izquierdo' }
  ];

  ngOnInit(): void {
    this.form = this.fb.group({
      casaId: [this.waterHouse?.casaId, Validators.required],
      seccionId: [null],
      calleId: [this.waterHouse?.calleId],
      casaNo: [this.waterHouse?.casaNo, Validators.required],
      nombre: [this.waterHouse?.nombre],
      lado: [this.waterHouse?.lado],
      observaciones: [this.waterHouse?.observaciones]
    });
    this.cargarSeccionesYCalles();
  }

  private cargarSeccionesYCalles(): void {
    this.catalogService.getOptionsByClave('SECCIONES_COLONIA').subscribe({
      next: (opts) => this.secciones = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptions(15).subscribe({
      next: (opts) => {
        this.todasLasCalles = opts;
        // Precargar la sección de la calle que ya tiene esta casa, para
        // que el dropdown de Calle no aparezca vacío al editar.
        const calleActual = opts.find(c => c.catalogoOpcionesId === this.waterHouse?.calleId);
        if (calleActual?.zonaId) {
          this.form.patchValue({ seccionId: calleActual.zonaId });
          this.onSeccionChange(calleActual.zonaId);
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  onSeccionChange(seccionId: number | null): void {
    if (!seccionId) {
      this.callesDeSeccion = [];
      return;
    }
    this.callesDeSeccion = this.todasLasCalles
      .filter(c => c.zonaId === seccionId)
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  save(): void {
    if (this.form.invalid) return;
    // seccionId es solo un filtro en pantalla para llegar a la calle
    // correcta -- agua_casa no guarda sección, así que no se manda.
    const { seccionId, ...payload } = this.form.value;
    this.formSubmit.emit(payload);
  }
}
