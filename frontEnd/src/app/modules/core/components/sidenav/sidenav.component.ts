import { MediaMatcher } from '@angular/cdk/layout';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.css'],
})
export class SidenavComponent implements OnInit {
  mobileQuery: MediaQueryList;
  username:any;

  menuNav = [
    { name: 'Home',       route: 'home',        icon: 'home' },
    { name: 'Valvulas',   route: 'waterValves',  icon: 'location_on' },
    { name: 'Casas',      route: 'houseList',    icon: 'house' },
    { name: 'Grupos',     route: 'groupList',    icon: 'groups' },
    { name: 'Personas',   route: 'personList',   icon: 'person' },
    { name: 'Usuarios',   route: 'user',         icon: 'manage_accounts' },
    { name: 'Pagos',      route: 'receipt',      icon: 'paid' },
    { name: 'Historial',  route: 'receiptHistory', icon: 'receipt' },
    { name: 'Convenios',  route: 'convenioList', icon: 'handshake' },
    { name: 'Cuotas',     route: 'cuotaList',    icon: 'request_quote' },
    { name: 'Asambleas',  route: 'assemblyList', icon: 'groups' },
    { name: 'Delegación', route: 'peopleList',   icon: 'group' },
    { name: 'Catálogos',  route: 'catalogList',  icon: 'library_books' },
  ];

  constructor(media: MediaMatcher) {
    this.mobileQuery = media.matchMedia('(max-witdth:600px)');
  }

  ngOnInit(): void {
    this.username = "";
  }

  logout() {
    //this.keycloakservice.logout();
  }
}
