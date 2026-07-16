import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs/operators';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';

@Component({
  selector: 'app-house-new-user',
  templateUrl: './house-new.component.html',
  styleUrls: ['./house-new.component.css']
})
export class HouseNewComponent implements OnInit {

  private readonly catalogService = inject(CatalogService);
  private readonly houseService   = inject(HouseService);
  private readonly fb             = inject(FormBuilder);
  private readonly dialogRef      = inject(MatDialogRef<HouseNewComponent>);

  waterHouse!: WaterHouseModel;
  form!: FormGroup;

  // Catálogo de calles (id 15), en orden alfabético
  calles$ = this.catalogService.getOptions(15).pipe(
    map(opts => [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)))
  );

  readonly lados = [
    { value: 'D', label: 'Derecho' },
    { value: 'I', label: 'Izquierdo' }
  ];

  // Mapa default (León, Los López)
  center           = { lat: 21.0447844, lng: -101.5706873 };
  zoom             = 15;
  markerPosition   = { lat: 21.0447844, lng: -101.5706873 };

  ngOnInit(): void {
    this.form = this.fb.group({
      casaId:        [null],
      calleId:       [null, Validators.required],
      casaNo:        [null, Validators.required],
      nombre:        [''],
      lado:          [''],
      observaciones: [''],
      lat:           [this.center.lat],
      lng:           [this.center.lng]
    });
  }

  onMapClick(event: google.maps.MapMouseEvent): void {
    if (event.latLng) {
      this.markerPosition = {
        lat: event.latLng.lat(),
        lng: event.latLng.lng()
      };
      this.form.patchValue({
        lat: this.markerPosition.lat,
        lng: this.markerPosition.lng
      });
    }
  }

  save(): void {
    if (this.form.invalid) return;
    this.houseService.addHouse(this.form.value).subscribe({
      next: () => this.dialogRef.close(true),
      error: (e: any) => console.error('Error al crear casa', e)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
