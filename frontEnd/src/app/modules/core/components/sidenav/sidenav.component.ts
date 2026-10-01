import { MediaMatcher } from '@angular/cdk/layout';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../shared/services/auth.service';
import { ChangePasswordComponent } from '../change-password/change-password.component';
import { ConfiguracionService } from '../../../shared/services/configuracion.service';

interface MenuItem {
  name: string;
  route: string;
  icon: string;
  soloAdmin?: boolean;
}

interface MenuGroup {
  name: string;
  icon: string;
  soloAdmin?: boolean;
  items: MenuItem[];
}

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.css'],
})
export class SidenavComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly configuracionService = inject(ConfiguracionService);

  mobileQuery: MediaQueryList;
  username:any;

  // Nombre del comité configurable (módulo de Configuración) -- mientras
  // carga se muestra el valor por defecto del servicio, para no dejar el
  // título vacío.
  nombreComite$ = this.configuracionService.nombreComite$;

  // Agrupado en submenús -- el menú plano ya no cabía cómodo. El grupo
  // "Finanzas" y la pantalla de cuentas del sistema son soloAdmin: el rol
  // "USUARIO1" no los ve (ver visibleMenuGroups()/ngOnInit).
  private readonly menuGroups: MenuGroup[] = [
    {
      name: 'Operación', icon: 'water_drop', items: [
        { name: 'Home', route: 'home', icon: 'home' },
        { name: 'Red de distribución', route: 'waterValves', icon: 'location_on' },
        { name: 'Casas', route: 'houseList', icon: 'house' },
        { name: 'Grupos', route: 'groupList', icon: 'groups' },
        { name: 'Preregistro', route: 'preregistroList', icon: 'how_to_reg' },
        { name: 'Personas', route: 'personList', icon: 'person' },
        { name: 'Censo', route: 'censoResumen', icon: 'groups_2' },
      ]
    },
    {
      name: 'Usuarios y pagos', icon: 'paid', items: [
        { name: 'Usuarios', route: 'user', icon: 'manage_accounts' },
        { name: 'Pagos', route: 'receipt', icon: 'paid' },
        { name: 'Historial', route: 'receiptHistory', icon: 'receipt' },
        { name: 'Revisión de recibos', route: 'reciboRevision', icon: 'fact_check' },
        { name: 'Convenios', route: 'convenioList', icon: 'handshake' },
        { name: 'Cuotas', route: 'cuotaList', icon: 'request_quote' },
        { name: 'Deudores', route: 'deudorList', icon: 'money_off' },
        { name: 'Valores generales', route: 'valorGeneral', icon: 'price_change' },
      ]
    },
    // Todos los tipos de carta/aviso del Comité, agrupados aparte -- antes
    // vivían dentro de "Usuarios y pagos", pero ya no todos dependen de un
    // adeudo (Aviso de bomba y Actualización de padrón aplican a cualquier
    // usuario), así que tenerlos juntos bajo su propio menú es más claro.
    {
      name: 'Cartas', icon: 'mark_email_unread', items: [
        { name: 'Aviso informativo de adeudo', route: 'avisoInformativoAdeudo', icon: 'info' },
        { name: 'Cartas de adeudo', route: 'avisoAdeudo', icon: 'mark_email_unread' },
        { name: 'Aviso uso de bomba', route: 'avisoBomba', icon: 'water_damage' },
        { name: 'Actualización de padrón', route: 'avisoPadron', icon: 'assignment_ind' },
        { name: 'Responsables de pago', route: 'avisoResponsablePago', icon: 'groups' },
      ]
    },
    {
      name: 'Finanzas', icon: 'account_balance', soloAdmin: true, items: [
        { name: 'Totales por año', route: 'totalPorAnio', icon: 'bar_chart' },
        { name: 'Resumen anual', route: 'resumenAnual', icon: 'summarize' },
        { name: 'Gastos del mes', route: 'egresoGastos', icon: 'fact_check' },
        { name: 'Vales', route: 'egresoVales', icon: 'receipt_long' },
        { name: 'Egresos', route: 'egresoList', icon: 'payments' },
      ]
    },
    {
      name: 'Administración', icon: 'admin_panel_settings', items: [
        { name: 'Asambleas', route: 'assemblyList', icon: 'groups' },
        { name: 'Delegación', route: 'peopleList', icon: 'group' },
        { name: 'Catálogos', route: 'catalogList', icon: 'library_books' },
        { name: 'Configuración', route: 'configuracion', icon: 'settings' },
        { name: 'Usuarios del sistema', route: 'sistemaUsuarioList', icon: 'admin_panel_settings', soloAdmin: true },
      ]
    },
  ];

  // Calculado una sola vez en ngOnInit (no un getter) -- un getter que
  // arma arreglos/objetos nuevos en cada ciclo, leído dentro de un *ngFor,
  // ya causó antes un ciclo infinito de detección de cambios en otra
  // pantalla (ver la nota en water-valves.component.ts, valvulasDisponibles).
  visibleMenuGroups: MenuGroup[] = [];

  // Qué grupos están expandidos -- todos colapsados por default, que es
  // justo lo que se pidió para que el menú no se sienta tan largo.
  private gruposAbiertos = new Set<string>();

  constructor(media: MediaMatcher) {
    this.mobileQuery = media.matchMedia('(max-witdth:600px)');
  }

  ngOnInit(): void {
    this.username = this.authService.getNombre() || this.authService.getUsername() || '';
    const esAdmin = this.authService.isAdmin();
    this.visibleMenuGroups = this.menuGroups
      .filter(g => !g.soloAdmin || esAdmin)
      .map(g => ({ ...g, items: g.items.filter(i => !i.soloAdmin || esAdmin) }));

    // Trae el nombre del comité real (por defecto "Los Lopez" mientras
    // carga) para el título de la app -- ya no queda "Los Sauces" fijo.
    this.configuracionService.getAll().subscribe({
      error: (e) => console.error('No se pudo cargar el nombre del comité', e)
    });
  }

  toggleGrupo(nombre: string): void {
    if (this.gruposAbiertos.has(nombre)) {
      this.gruposAbiertos.delete(nombre);
    } else {
      this.gruposAbiertos.add(nombre);
    }
  }

  grupoAbierto(nombre: string): boolean {
    return this.gruposAbiertos.has(nombre);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openChangePassword(): void {
    this.dialog.open(ChangePasswordComponent, { width: '400px' });
  }
}
