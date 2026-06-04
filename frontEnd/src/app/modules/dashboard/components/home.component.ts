import { Component, OnInit, inject } from '@angular/core';
import { WaterUserModel } from '../../shared/models/WaterUser.model';
import { MatTableDataSource } from '@angular/material/table';
import { UserService } from '../../shared/services/user.service';
import { Chart } from 'chart.js';
import { ReplaySubject } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit{

  chartBar:any;
  chartDoughnutr:any;
  dataSource = new MatTableDataSource<WaterUserModel>();
  private userService = inject(UserService);


  ngOnInit(): void {
   this.getuserMoraCountByZona();
  }

  getuserMoraCountByZona():void{
    this.userService.getuserMoraCountByZona()
      .subscribe({
        next: (v) => this.processUserResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processUserResponse(resp:any){
    const estatus : String []= [];
    const total : number []= [];

    
    //const 
    if (resp.metadata[0].code == "00"){
      let map = resp.mapResponse.map;

      for (const clave in map) {
        if (Object.prototype.hasOwnProperty.call(map, clave)) {
          estatus.push(clave);
          total.push(map[clave]);
        }
      }

      this.chartBar = new Chart('canvas-bar',{
        type:'bar',
        data:{
          labels:estatus,
          datasets:
          [ {label: 'Estatus Mora', data:total}]
           
        }
      });

      this.chartDoughnutr = new Chart('canvas-doughnut',{
        type:'doughnut',
        data:{
          labels:estatus,
          datasets:
          [ {label: 'Estatus Mora', data:total}]
           
        }
      });
      
    }
  }

}
