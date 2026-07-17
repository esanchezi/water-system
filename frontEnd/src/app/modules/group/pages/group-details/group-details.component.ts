import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { WaterGroupModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.css'],
})
export class GroupDetailsComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);

  waterGroup!: WaterGroupModel;
  listWaterUser: WaterUserModel[] = [];

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterGroup = JSON.parse(params['element']);
      }
      this.inicializarUsuarios();
    });
  }

  private inicializarUsuarios(): void {
    this.listWaterUser = this.waterGroup?.listWaterUser || [];
  }

  trackByUser(index: number, user: WaterUserModel): number {
    return user.aguaUsuarioId;
  }

}
