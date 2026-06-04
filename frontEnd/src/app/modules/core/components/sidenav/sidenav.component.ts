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
    { name: 'Home', route: 'home', icon: 'home' },
    { name: 'Valvulas', route: 'waterValves', icon: 'location_on' },
    { name: 'Casas', route: 'houseList', icon: 'group' },
    { name: 'Grupos', route: 'groupList', icon: 'group' },
    { name: 'Personas', route: 'personList', icon: 'group' },
    { name: 'Usuarios', route: 'user', icon: 'group' },
    { name: 'Pagos', route: 'receipt', icon: 'paid' },
    { name: 'Historial', route: 'receiptHistory', icon: 'receipt' },
    { name: 'Asambleas', route: 'assemblyList', icon: 'group' },
    { name: 'Delegación', route: 'peopleList', icon: 'group' },
    
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
