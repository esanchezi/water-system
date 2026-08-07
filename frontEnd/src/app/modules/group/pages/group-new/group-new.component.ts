import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { GroupService } from 'src/app/modules/shared/services/group.service';

@Component({
  selector: 'app-group-new',
  templateUrl: './group-new.component.html',
  styleUrls: ['./group-new.component.css']
})
export class GroupNewComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly groupService = inject(GroupService);
  private readonly dialogRef = inject(MatDialogRef<GroupNewComponent>);

  form!: FormGroup;
  guardando = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      observaciones: ['']
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.guardando = true;
    this.groupService.addGroup(this.form.value).subscribe({
      next: () => {
        this.guardando = false;
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        this.guardando = false;
        console.error('Error al crear el grupo', e);
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
