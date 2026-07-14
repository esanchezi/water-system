import { Component, Inject, OnInit, inject, Optional } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { WaterUserChargeModel } from 'src/app/modules/shared/models/WaterUserCharge.model';
import { AgreementService } from 'src/app/modules/shared/services/agreement.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { UserChargeService } from 'src/app/modules/shared/services/user-charge.service';
import { UserService } from 'src/app/modules/shared/services/user.service';

// Cuando se abre desde la ficha de usuario ya se manda el usuario, y no
// hace falta el autocomplete (mismo patrón que AssemblyNewAttendanceComponent).
export interface NewConvenioDialogData {
  noUsuario?: number;
  nombreUsuario?: string;
}

@Component({
  selector: 'app-new-convenio',
  templateUrl: './new-convenio.component.html',
  styleUrls: ['./new-convenio.component.css']
})
export class NewConvenioComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly agreementService = inject(AgreementService);
  private readonly userChargeService = inject(UserChargeService);
  private readonly userService = inject(UserService);
  private readonly catalogService = inject(CatalogService);

  public convenioForm!: FormGroup;
  usuariosFiltrados: any[] = [];
  usuarioSeleccionado: any;
  cargosPendientes: WaterUserChargeModel[] = [];
  estatusConvenio: CatalogOptionModel[] = [];

  constructor(
    public dialogRef: MatDialogRef<NewConvenioComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: NewConvenioDialogData | null
  ) { }

  ngOnInit(): void {
    this.convenioForm = this.fb.group({
      noUsuario:         ['', Validators.required],
      noFolio:           [''], // opcional: no todos los convenios llevan folio del talonario
      fecha:             ['', Validators.required],
      motivo:            ['', Validators.required],
      comentario:        [''],
      estatusConvenioId: [''],
      lineasArray:       this.fb.array([])
    });

    this.catalogService.getOptionsByClave('ESTATUS_CONVENIO').subscribe({
      next: (opts) => this.estatusConvenio = opts,
      error: (e: any) => console.error(e)
    });

    const dialogData = this.data;
    if (dialogData?.noUsuario) {
      // Ya viene el usuario desde la ficha de usuario: se salta el autocomplete.
      this.usuarioSeleccionado = { noUsuario: dialogData.noUsuario, nombreCompleto: dialogData.nombreUsuario };
      this.convenioForm.get('noUsuario')?.setValue(dialogData.noUsuario);
      this.loadCargosPendientes(dialogData.noUsuario);
    } else {
      this.initUsuarioAutocomplete();
    }
  }

  private initUsuarioAutocomplete(): void {
    this.convenioForm.get('noUsuario')?.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(value => {
        if (!value || typeof value !== 'string') return;
        this.userService.searchUsersByNumber(value).subscribe((resp: any) => {
          this.usuariosFiltrados = resp;
        });
      });
  }

  displayUsuario(user: any): string {
    return user ? `${user.noUsuario} - ${user.nombreCompleto}` : '';
  }

  onUsuarioSelected(user: any): void {
    this.usuarioSeleccionado = user;
    this.loadCargosPendientes(user.noUsuario);
  }

  get lineasArray(): FormArray {
    return this.convenioForm.get('lineasArray') as FormArray;
  }

  private loadCargosPendientes(noUsuario: number): void {
    this.lineasArray.clear();
    this.userChargeService.getPendientesByUser(noUsuario).subscribe({
      next: (resp: any) => {
        if (resp.metadata[0].code !== '00') return;
        this.cargosPendientes = resp.data;
        this.cargosPendientes.forEach(cargo => {
          this.lineasArray.push(this.fb.group({
            aguaUsuarioCargoId: [cargo.aguaUsuarioCargoId],
            concepto:           [cargo.concepto?.nombre],
            saldo:              [cargo.saldo],
            montoCondonado:     [0]
          }));
        });
      },
      error: (e: any) => console.error(e)
    });
  }

  onSave(): void {
    const form = this.convenioForm.value;
    const cargos = (form.lineasArray as any[])
      .filter(l => Number(l.montoCondonado) > 0)
      .map(l => ({ aguaUsuarioCargoId: l.aguaUsuarioCargoId, montoCondonado: l.montoCondonado }));

    const data = {
      noUsuario:         this.usuarioSeleccionado?.noUsuario,
      noFolio:            form.noFolio || null,
      fecha:              form.fecha,
      motivo:             form.motivo,
      comentario:         form.comentario,
      estatusConvenioId:  form.estatusConvenioId || null,
      cargos
    };

    this.agreementService.save(data).subscribe({
      next: () => this.dialogRef.close(1),
      error: () => this.dialogRef.close(2)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
