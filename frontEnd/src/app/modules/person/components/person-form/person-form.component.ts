import { Component, Input, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';

@Component({
  selector: 'app-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['./person-form.component.css']
})
export class PersonFormComponent implements OnInit {

  @Input() person!: PersonModel;

  private readonly fb = inject(FormBuilder);

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      personaId: [this.person?.personaId, Validators.required],
      nombre: [this.person?.nombre],
      app: [this.person?.app, Validators.required],
      apm: [this.person?.apm],
    });
  }
}