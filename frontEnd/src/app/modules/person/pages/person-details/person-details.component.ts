import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { UserService } from 'src/app/modules/shared/services/user.service';

@Component({
  selector: 'app-person-details',
  templateUrl: './person-details.component.html',
  styleUrls: ['./person-details.component.css']
})
export class PersonDetailsComponent implements OnInit{

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  person!: PersonModel;
  listWaterUser: WaterUserModel[] = [];

   constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.person = JSON.parse(params['element']);
      }
     this.loadUsers(this.person.personaId);
    });
  }

  loadUsers(idPersona: number): void {
  this.userService.getUsersByPersonId(idPersona).subscribe({
    next: (resp: any) => {
      this.listWaterUser = resp.data;  // 👈 tomar solo el array
    },
    error: (err) => {
      console.error('Error cargando usuarios:', err);
    }
  });
}
  trackByUser(index: number, user: WaterUserModel): number {
    return user.id;
  }

  // Link directo a la ficha completa del usuario (details-user vuelve a
  // pedir todos los datos con solo el usuarioId).
  irADetalleUsuario(user: WaterUserModel): void {
    this.router.navigate(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: user.aguaUsuarioId }) }
    });
  }

}
