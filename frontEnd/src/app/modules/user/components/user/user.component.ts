import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { UserService } from '../../../shared/services/user.service';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { NewUserComponent } from '../new-user/new-user.component';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { WaterUserBasicModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { MatPaginator } from '@angular/material/paginator';
import { UtilService } from 'src/app/modules/shared/services/util.service';
import { ReceiptService } from '../../../shared/services/receipt.service';
import { Router } from '@angular/router';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit{
 
  private readonly userService = inject(UserService);
  private readonly receiptService = inject(ReceiptService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly util = inject(UtilService);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  public dialog = inject(MatDialog);
  displayColumns: string[]=['noUser','nombre','direccion','casa','observaciones','actions'];
  dataSource = new MatTableDataSource<WaterUserBasicModel>();
  nombreFiltro: string = '';
  apellidoFiltro: string = '';
  noUserFiltro: string = '';
  calleIdFiltro: number | null = null;
  calleNombreFiltro: string = '';
  casaNoFiltro: string = '';

  // Catálogo de calles (id 15), en orden alfabético, para el filtro por calle
  calles: CatalogOptionModel[] = [];
  //isAdmin:any;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data: WaterUserBasicModel, filter: string) => {
      const searchTerms = JSON.parse(filter);

      const nombre = data.nombre?.toLowerCase() || '';
      const apellido = data.app?.toLowerCase() || '';
      const noUser = data.noUsuario?.toString() || '';
      const calleId = data.calleId?.toString() || '';
      const calleTexto = data.calleTexto?.toLowerCase() || '';
      const casaNo = data.casaNo?.toString() || '';
      const numeroTexto = data.numeroTexto?.toString() || '';

      // La calle puede venir de dos lugares: la casa asignada en el catálogo
      // (calleId) o, si el usuario aún no tiene casa asignada, del texto libre
      // de su domicilio (calleTexto). Por eso se acepta cualquiera de los dos.
      const matchCalle = !searchTerms.calleId ||
        calleId === searchTerms.calleId ||
        calleTexto.includes(searchTerms.calleNombre);

      // Mismo caso para el número de casa: casaNo (catálogo) o numeroTexto (domicilio libre).
      const matchCasa = !searchTerms.casaNo ||
        casaNo.includes(searchTerms.casaNo) ||
        numeroTexto.includes(searchTerms.casaNo);

      return nombre.includes(searchTerms.nombre) &&
            apellido.includes(searchTerms.apellido) &&
            noUser.includes(searchTerms.noUser) &&
            matchCalle &&
            matchCasa;
    };

    this.loadCalles();
    this.getUsers();
  }

  private loadCalles(): void {
    this.catalogService.getOptions(15).subscribe({
      next: (opts) => this.calles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
  }

  getUsers():void{
    this.userService.getBasicUsers()
      .subscribe({
        next: (v) => this.processUserResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processUserResponse(resp:any){
    const dataUser: WaterUserBasicModel[] = [];
    if (resp.metadata[0].code == "00"){
      let listUser = resp.data;

      listUser.forEach((element:WaterUserBasicModel)=> {
        dataUser.push(element);
      });
      this.dataSource.data = listUser;
      this.dataSource.paginator = this.paginator;
    }
  }

  openUserDialog(){
    const dialogRef = this.dialog.open(NewUserComponent,{
      width:'900px'
    });

    dialogRef.afterClosed().subscribe((result:any) => {
      if(result == 1){
        this.openSnackBar("Categoria agregada","Exito")
        this.getUsers();
      }else if (result == 2){
        this.openSnackBar("Error al guardar categoria","Error")
      }
    });
  }

  aplicarFiltro() {
    this.dataSource.filter = JSON.stringify({
      nombre: this.nombreFiltro.trim().toLowerCase(),
      apellido: this.apellidoFiltro.trim().toLowerCase(),
      noUser: this.noUserFiltro.trim(),
      calleId: this.calleIdFiltro != null ? String(this.calleIdFiltro) : '',
      calleNombre: this.calleNombreFiltro.trim().toLowerCase(),
      casaNo: this.casaNoFiltro.trim()
    });
  }

  applyCalleCatalogoFiltro(calleId: number | null): void {
    this.calleIdFiltro = calleId;
    this.calleNombreFiltro = calleId != null
      ? (this.calles.find(c => c.catalogoOpcionesId === calleId)?.nombre ?? '')
      : '';
    this.aplicarFiltro();
  }

  buscar(termino:string){
    if(termino.length === 0){
      return this.getUsers();
    }

    this.userService.getUsersByName(termino)
    .subscribe({
      next: (v) => this.processUserResponse(v)
    });
  }

  buscarNoUser(termino:string){
    if(termino.length === 0){
      return this.getUsers();
    }
    this.userService.getUsersByNoUser(termino)
    .subscribe({
      next: (v) => this.processUserResponse(v)
    });

  }
  buscarApp(nombre:string, app:string){
    if(app.length === 0){
      return this.getUsers();
    }
    this.userService.getUsersByNameApp(nombre,app)
    .subscribe({
      next: (v) => this.processUserResponse(v)
    });

  }

  buscarCalle(calle:string){
    if(calle.length === 0){
      return this.getUsers();
    }
    this.userService.getUsersByStreet(calle)
    .subscribe({
      next: (v) => this.processUserResponse(v)
    });

  }

  edit(element:any){
    console.log(element);
    this.router.navigate(['dashboard/detailsUser'],{
      queryParams: { element: JSON.stringify(element) },
      // skipLocationChange: true,
    });

  }

  openSnackBar(message:string, action:string): MatSnackBarRef<SimpleSnackBar>{
    return this.snackBar.open(message,action,{duration:2000})
  }

  exportExcel(){
    this.receiptService.exportReportReceipt()
    .subscribe({
      next: (v) => {
        let file= new Blob([v],{type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
        let fileUrl = URL.createObjectURL(file);
        var anchor = document.createElement("a");
        anchor.download = "categories.xlsx";
        anchor.href = fileUrl;
        anchor.click();

        this.openSnackBar("Archivo exportado correctamente", "Exitosa");
      },
      error: (e) => this.openSnackBar("No se pudo exportar el archivo", "Error")
    });
  }
}



