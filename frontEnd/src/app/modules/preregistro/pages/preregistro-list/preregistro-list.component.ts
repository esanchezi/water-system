import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

import {
  PREREGISTRO_ESTATUS_CONVERTIDO,
  PREREGISTRO_ESTATUS_DESCARTADO,
  PREREGISTRO_ESTATUS_PENDIENTE,
  PreregistroUsuarioModel
} from 'src/app/modules/shared/models/PreregistroUsuario.model';
import { PreregistroUsuarioService } from 'src/app/modules/shared/services/preregistro-usuario.service';
import { WaterGroupModel } from 'src/app/modules/shared/models/WaterUser.model';
import { GroupService } from 'src/app/modules/shared/services/group.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { NewUserComponent } from 'src/app/modules/user/components/new-user/new-user.component';

// Listado global (todas las casas) de gente en preregistro -- para no
// tener que entrar casa por casa a revisar quién sigue pendiente, quién ya
// se descartó, o para asignarle de una vez el grupo al que probablemente
// se va a unir cuando se convierta en usuario formal.
@Component({
  selector: 'app-preregistro-list',
  templateUrl: './preregistro-list.component.html',
  styleUrls: ['./preregistro-list.component.css']
})
export class PreregistroListComponent implements OnInit {

  private readonly preregistroService = inject(PreregistroUsuarioService);
  private readonly groupService = inject(GroupService);
  private readonly houseService = inject(HouseService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  readonly PENDIENTE = PREREGISTRO_ESTATUS_PENDIENTE;
  readonly CONVERTIDO = PREREGISTRO_ESTATUS_CONVERTIDO;
  readonly DESCARTADO = PREREGISTRO_ESTATUS_DESCARTADO;

  displayColumns: string[] = ['casa', 'nombre', 'estatus', 'negocio', 'deuda', 'grupo', 'acciones'];
  dataSource = new MatTableDataSource<PreregistroUsuarioModel>();
  grupos: WaterGroupModel[] = [];

  cargando = false;
  cargandoCasaId: number | null = null;

  filtros = {
    nombre: '',
    estatus: '',
    soloNegocio: false
  };

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
    this.groupService.getListWaterGroup().subscribe({
      next: (resp: any) => {
        // OJO: waterGroup usa BaseRestResponse (metadata como objeto), no
        // como lista -- distinto del resto de los endpoints de este
        // proyecto que usan RestResponse (metadata como arreglo).
        if (resp.metadata?.code === '00') {
          this.grupos = resp.data || [];
        }
      },
      error: (e: any) => console.error('Error al cargar grupos', e)
    });
  }

  load(): void {
    this.cargando = true;
    this.preregistroService.getAll().subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.[0]?.code === '00') {
          const data: PreregistroUsuarioModel[] = resp.data || [];
          this.dataSource = new MatTableDataSource<PreregistroUsuarioModel>(data);
          this.dataSource.paginator = this.paginator;
          this.dataSource.filterPredicate = (row: PreregistroUsuarioModel, filter: string) => {
            const f = JSON.parse(filter);
            const texto = `${row.nombre} ${row.telefono || ''} ${row.calleNombre || ''}`.toLowerCase();
            const matchNombre = !f.nombre || texto.includes(f.nombre);
            const matchEstatus = !f.estatus || row.estatus === Number(f.estatus);
            const matchNegocio = !f.soloNegocio || row.esNegocio === true;
            return matchNombre && matchEstatus && matchNegocio;
          };
          this.applyFilters();
        }
      },
      error: (e: any) => {
        this.cargando = false;
        console.error('Error al cargar el preregistro', e);
      }
    });
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filtros);
  }

  applyNombreFilter(event: Event): void {
    this.filtros.nombre = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.applyFilters();
  }

  totalDeuda(p: PreregistroUsuarioModel): number {
    return (Number(p.deudaAportaciones) || 0) + (Number(p.deudaMultasRecargos) || 0);
  }

  // Abre la ficha de la casa (equivalente a house-list.edit()), cargando
  // primero los datos completos por id para que house-details no se quede
  // con campos vacíos (no vuelve a recargar por su cuenta al navegar).
  verCasa(item: PreregistroUsuarioModel): void {
    if (!item.casaId) return;
    this.cargandoCasaId = item.casaId;
    this.houseService.getWaterHouseById(item.casaId).subscribe({
      next: (resp: any) => {
        this.cargandoCasaId = null;
        if (resp.metadata?.code === '00') {
          const casa = resp.data?.[0];
          if (casa) {
            this.router.navigate(['dashboard/houseDetails'], {
              queryParams: { element: JSON.stringify(casa) },
            });
          }
        }
      },
      error: (e: any) => {
        this.cargandoCasaId = null;
        console.error('Error al cargar la casa', e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo abrir la casa.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  estatusNombre(estatus: number): string {
    if (estatus === this.CONVERTIDO) return 'Convertido';
    if (estatus === this.DESCARTADO) return 'Descartado';
    return 'Pendiente';
  }

  // Asigna (o quita, seleccionando "Sin grupo") el grupo al que
  // probablemente se va a unir esta persona cuando se convierta en usuario.
  onAsignarGrupo(item: PreregistroUsuarioModel, grupoId: number | null): void {
    this.preregistroService.asignarGrupo(item.preregistroId, grupoId).subscribe({
      next: () => {
        item.grupoId = grupoId ?? null;
        const grupo = this.grupos.find(g => g.grupoId === grupoId);
        item.grupoNombre = grupo?.nombre;
      },
      error: (e: any) => {
        console.error(e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo asignar el grupo.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  // Abre el formulario normal de "Nuevo usuario", prellenado con el nombre
  // capturado en el preregistro (mismo flujo que en el detalle de la casa).
  onConvertirAUsuario(item: PreregistroUsuarioModel): void {
    const dialogRef = this.dialog.open(NewUserComponent, {
      width: '900px',
      data: {
        preregistroId: item.preregistroId,
        casaIdDestino: item.casaId,
        nombrePrellenado: item.nombre
      }
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === true) {
        Swal.fire({ icon: 'success', title: 'Usuario creado', text: 'El preregistro quedó marcado como convertido.', confirmButtonText: 'Aceptar' });
        this.load();
      }
    });
  }

  onMarcarDescartado(item: PreregistroUsuarioModel): void {
    Swal.fire({
      title: 'Descartar preregistro',
      text: '¿Por qué ya no se va a convertir en usuario? (opcional)',
      input: 'text',
      inputLabel: 'Motivo',
      showCancelButton: true,
      confirmButtonText: 'Descartar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.preregistroService.marcarDescartado(item.preregistroId, result.value).subscribe({
        next: () => this.load(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo descartar el registro.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }
}
