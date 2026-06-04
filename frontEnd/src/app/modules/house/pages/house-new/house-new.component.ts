import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { map } from 'rxjs';
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

  constructor(
    private houseService: HouseService,
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<HouseNewComponent>
  ) {}

  waterHouse!: WaterHouseModel;

  form!: FormGroup;

  zonas$ = this.catalogService.getCatalogByIdResponse(20).pipe(
    map(resp => resp.data[0].options)
  );

  // mapa default (León, Los López)
  center = { lat: 21.0447844, lng: -101.5706873 };
  zoom = 15;
  markerPosition = { lat: 21.0447844, lng: -101.5706873 };

  ngOnInit(): void {
   this.form = this.fb.group({
      casaId: [null],
      zonaId: [null, Validators.required],
      casaNo: [null, Validators.required],
      nombre: [''],
      observaciones: [''],
      lat: [this.center.lat],
      lng: [this.center.lng]
    });
  }

  onMapClick(event: google.maps.MapMouseEvent) {
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

  save() {
    if (this.form.invalid) return;

    const newHouse = this.form.value;
    this.houseService.addHouse(newHouse).subscribe({
      next: (resp) => {
        console.log('Casa creada:', resp);
        this.dialogRef.close(true); // ✅ sin warning
      },
      error: (err) => {
        console.error('Error al crear casa', err);
      }
    });
  }

  cancel(success = false) {
    if (this.dialogRef) {
      this.dialogRef.close(success);
    }
  }
}
