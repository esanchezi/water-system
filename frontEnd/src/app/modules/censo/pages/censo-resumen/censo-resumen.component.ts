import { Component, OnInit, inject } from '@angular/core';
import { WaterUserCensusResumenModel } from 'src/app/modules/shared/models/WaterUserCensus.model';
import { WaterUserCensusService } from 'src/app/modules/shared/services/water-user-census.service';

@Component({
  selector: 'app-censo-resumen',
  templateUrl: './censo-resumen.component.html',
  styleUrls: ['./censo-resumen.component.css']
})
export class CensoResumenComponent implements OnInit {

  private readonly censusService = inject(WaterUserCensusService);

  resumen: WaterUserCensusResumenModel | null = null;
  cargando = false;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.cargando = true;
    this.censusService.getResumenPorEdad().subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.[0]?.code === '00') {
          this.resumen = resp.data?.[0] ?? null;
        }
      },
      error: (e: any) => {
        this.cargando = false;
        console.error('Error al cargar el resumen del censo', e);
      }
    });
  }
}
