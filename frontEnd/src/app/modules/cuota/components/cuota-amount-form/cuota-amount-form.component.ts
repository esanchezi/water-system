import { Component, inject, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { FeeAmountModel } from 'src/app/modules/shared/models/Fee.model';

export interface CuotaAmountDialogData {
  cuotaId: number;
  amount: FeeAmountModel | null;
}

@Component({
  selector: 'app-cuota-amount-form',
  templateUrl: './cuota-amount-form.component.html',
  styleUrls: ['./cuota-amount-form.component.css']
})
export class CuotaAmountFormComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly feeService = inject(FeeService);
  private readonly dialogRef = inject(MatDialogRef<CuotaAmountFormComponent>);

  form!: FormGroup;
  isEdit = false;
  saving = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CuotaAmountDialogData) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.amount;
    this.form = this.fb.group({
      vigencia: [
        this.data.amount?.vigencia ?? new Date().getFullYear(),
        [Validators.required, Validators.min(2000), Validators.max(2100)]
      ],
      cuota: [this.data.amount?.cuota ?? null, [Validators.required, Validators.min(0)]],
      observaciones: [this.data.amount?.observaciones ?? '']
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const body = this.form.value;
    const { cuotaId, amount } = this.data;

    const request$ = this.isEdit
      ? this.feeService.updateAmount(cuotaId, amount!.cuotaMontoId, body)
      : this.feeService.addAmount(cuotaId, body);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        console.error(e);
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
