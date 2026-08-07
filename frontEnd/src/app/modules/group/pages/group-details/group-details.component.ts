import { Component, inject, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { ActivatedRoute } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { WaterGroupModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { WaterUserChargeModel } from 'src/app/modules/shared/models/WaterUserCharge.model';
import { UserChargeService } from 'src/app/modules/shared/services/user-charge.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { GroupService } from 'src/app/modules/shared/services/group.service';
import { PreregistroUsuarioModel } from 'src/app/modules/shared/models/PreregistroUsuario.model';
import { PreregistroUsuarioService } from 'src/app/modules/shared/services/preregistro-usuario.service';
import Swal from 'sweetalert2';

// Mismo shape que devuelve GET /waterUser/search -- busca por nombre,
// apellido, alias O número de usuario a la vez (ver house-details, donde se
// usa este mismo patrón para agregar un usuario a una casa).
interface UserSearchResult {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;
}

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.css'],
})
export class GroupDetailsComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly userChargeService = inject(UserChargeService);
  private readonly preregistroService = inject(PreregistroUsuarioService);
  private readonly userService = inject(UserService);
  private readonly groupService = inject(GroupService);

  waterGroup!: WaterGroupModel;
  listWaterUser: WaterUserModel[] = [];

  // Buscador para agregar un usuario YA EXISTENTE a este grupo -- por
  // nombre, alias O N° de usuario (antes no había ninguna opción para
  // hacer esto desde la ficha del grupo).
  userSearchCtrl = new FormControl('');
  userSearchResults: UserSearchResult[] = [];
  agregandoUsuario = false;

  // Buscador para vincular un preregistro (gente sin usuario formal
  // todavía) a este grupo -- solo por nombre, ya que el preregistro no
  // tiene N° de usuario.
  preregistroSearchCtrl = new FormControl('');
  preregistroSearchResults: PreregistroUsuarioModel[] = [];
  private todoElPreregistro: PreregistroUsuarioModel[] = [];
  agregandoPreregistroBusqueda = false;

  // Cargos (multas, recargos, etc.) de todos los usuarios del grupo juntos,
  // para no tener que entrar usuario por usuario a revisarlos.
  listMultasGrupo: WaterUserChargeModel[] = [];
  cargandoMultas = false;
  totalSaldoMultasGrupo = 0;

  // Gente en preregistro que probablemente se va a unir a este grupo
  // cuando se convierta en usuario formal.
  listPreregistroGrupo: PreregistroUsuarioModel[] = [];
  cargandoPreregistro = false;
  totalDeudaPreregistroGrupo = 0;

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.waterGroup = JSON.parse(params['element']);
      }
      this.inicializarUsuarios();
    });

    this.userSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const value = typeof term === 'string' ? term.trim() : '';
        if (value.length < 2) return of([]);
        return this.userService.searchUsersByNumber(value);
      })
    ).subscribe({
      next: (results: UserSearchResult[]) => this.userSearchResults = results || [],
      error: () => this.userSearchResults = []
    });

    // El preregistro no tiene un endpoint de búsqueda propio -- se carga
    // una vez completo (lista acotada a toda la colonia) y se filtra en el
    // cliente, igual que hace preregistro-list con su buscador.
    this.preregistroService.getAll().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code === '00') {
          this.todoElPreregistro = resp.data || [];
        }
      },
      error: (e: any) => console.error('Error al cargar el preregistro', e)
    });

    this.preregistroSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      const value = typeof term === 'string' ? term.trim().toLowerCase() : '';
      this.preregistroSearchResults = value.length < 2
        ? []
        : this.todoElPreregistro.filter(p => p.nombre?.toLowerCase().includes(value));
    });
  }

  private inicializarUsuarios(): void {
    this.listWaterUser = this.waterGroup?.listWaterUser || [];
  }

  trackByUser(index: number, user: WaterUserModel): number {
    return user.aguaUsuarioId;
  }

  // Texto mostrado en el input del autocomplete al seleccionar un usuario
  displayUser = (user: UserSearchResult): string => {
    return user?.nombreCompleto ? `N° ${user.noUsuario} - ${user.nombreCompleto}` : '';
  };

  // Asigna al usuario elegido este grupo, y recarga la ficha del grupo
  // completa (así se refleja de una vez en el acordeón de usuarios sin
  // tener que volver a entrar a la pantalla).
  onUserSelected(event: MatAutocompleteSelectedEvent): void {
    const user: UserSearchResult = event.option.value;
    if (!this.waterGroup?.grupoId || !user?.aguaUsuarioId) return;

    this.agregandoUsuario = true;
    this.userService.assignGroup(user.aguaUsuarioId, this.waterGroup.grupoId).subscribe({
      next: () => {
        this.userSearchCtrl.setValue('');
        this.userSearchResults = [];
        this.recargarGrupo();
      },
      error: (e: any) => {
        this.agregandoUsuario = false;
        console.error('Error al agregar el usuario al grupo', e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo agregar el usuario al grupo.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  displayPreregistro = (p: PreregistroUsuarioModel): string => {
    return p?.nombre ?? '';
  };

  // Vincula el preregistro elegido a este grupo (mismo endpoint que ya usa
  // el módulo de Preregistro) y recarga el panel "Preregistro vinculado".
  onPreregistroSelected(event: MatAutocompleteSelectedEvent): void {
    const item: PreregistroUsuarioModel = event.option.value;
    if (!this.waterGroup?.grupoId || !item?.preregistroId) return;

    this.agregandoPreregistroBusqueda = true;
    this.preregistroService.asignarGrupo(item.preregistroId, this.waterGroup.grupoId).subscribe({
      next: () => {
        this.agregandoPreregistroBusqueda = false;
        this.preregistroSearchCtrl.setValue('');
        this.preregistroSearchResults = [];
        this.getPreregistroGrupo();
      },
      error: (e: any) => {
        this.agregandoPreregistroBusqueda = false;
        console.error('Error al vincular el preregistro al grupo', e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo vincular el preregistro al grupo.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  private recargarGrupo(): void {
    this.groupService.getWaterGroupById(this.waterGroup.grupoId).subscribe({
      next: (resp: any) => {
        this.agregandoUsuario = false;
        if (resp.metadata?.code === '00') {
          const data = resp.data?.[0];
          if (data) {
            this.waterGroup = data;
            this.inicializarUsuarios();
          }
        }
      },
      error: (e: any) => {
        this.agregandoUsuario = false;
        console.error('Error al recargar el grupo', e);
      }
    });
  }

  // El panel "Multas del grupo" trae ambas fuentes juntas: los cargos de
  // usuarios reales Y la deuda aproximada de preregistro (que no tiene
  // usuario todavía, pero sí se sabe que debe algo).
  getMultasGrupo(): void {
    if (!this.waterGroup?.grupoId) return;
    this.cargandoMultas = true;
    this.userChargeService.getChargesByGrupo(this.waterGroup.grupoId).subscribe({
      next: (resp: any) => {
        this.cargandoMultas = false;
        if (resp.metadata?.[0]?.code === '00') {
          const data: WaterUserChargeModel[] = resp.data || [];
          this.listMultasGrupo = data;
          this.totalSaldoMultasGrupo = data.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);
        }
      },
      error: (e: any) => {
        this.cargandoMultas = false;
        console.error('Error al cargar las multas del grupo', e);
      }
    });
    this.getPreregistroGrupo();
  }

  getPreregistroGrupo(): void {
    if (!this.waterGroup?.grupoId) return;
    this.cargandoPreregistro = true;
    this.preregistroService.getByGrupoId(this.waterGroup.grupoId).subscribe({
      next: (resp: any) => {
        this.cargandoPreregistro = false;
        if (resp.metadata?.[0]?.code === '00') {
          const data: PreregistroUsuarioModel[] = resp.data || [];
          this.listPreregistroGrupo = data;
          this.totalDeudaPreregistroGrupo = data.reduce(
            (acc, p) => acc + (Number(p.deudaAportaciones) || 0) + (Number(p.deudaMultasRecargos) || 0), 0
          );
        }
      },
      error: (e: any) => {
        this.cargandoPreregistro = false;
        console.error('Error al cargar el preregistro del grupo', e);
      }
    });
  }

  // Filas combinadas para la tabla de "Multas del grupo": una fila por
  // cargo real, más una fila por cada preregistro vinculado con su deuda
  // aproximada -- así la columna "Usuario" muestra usuario o preregistro
  // indistintamente y el total de abajo ya queda combinado.
  get filasMultasYPreregistro(): any[] {
    const filasUsuarios = this.listMultasGrupo.map(c => ({
      usuarioLabel: `${c.nombreUsuario} (N° ${c.noUsuario})`,
      concepto: c.concepto?.nombre,
      fecha: c.fechaStr,
      monto: c.monto,
      saldo: c.saldo
    }));
    const filasPreregistro = this.listPreregistroGrupo.map(p => ({
      usuarioLabel: `${p.nombre} (Preregistro)`,
      concepto: 'Deuda aproximada',
      fecha: '-',
      monto: (Number(p.deudaAportaciones) || 0) + (Number(p.deudaMultasRecargos) || 0),
      saldo: (Number(p.deudaAportaciones) || 0) + (Number(p.deudaMultasRecargos) || 0)
    }));
    return [...filasUsuarios, ...filasPreregistro];
  }

  // Total combinado: multas de usuarios reales + deuda aproximada de
  // preregistro. Es el total que se muestra al pie de "Multas del grupo".
  get totalGeneralGrupo(): number {
    return this.totalSaldoMultasGrupo + this.totalDeudaPreregistroGrupo;
  }

}
