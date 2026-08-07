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
  displayColumns: string[]=['noUser','nombre','alias','direccion','casa','observaciones','actions'];
  dataSource = new MatTableDataSource<WaterUserBasicModel>();
  nombreFiltro: string = '';
  aliasFiltro: string = '';
  noUserFiltro: string = '';
  calleIdFiltro: number | null = null;
  calleNombreFiltro: string = '';
  casaNoFiltro: string = '';
  seccionIdFiltro: number | null = null;

  // Catálogo de secciones (SECCIONES_COLONIA) y de calles (id 15), en orden
  // alfabético, para la cascada de filtros Sección -> Calle (mismo patrón
  // que house-list/house-new).
  secciones: CatalogOptionModel[] = [];
  private todasLasCalles: CatalogOptionModel[] = [];
  calles: CatalogOptionModel[] = [];
  private zonaPorCalleId = new Map<number, number>();
  //isAdmin:any;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data: WaterUserBasicModel, filter: string) => {
      const searchTerms = JSON.parse(filter);

      // Nombre completo unificado (antes eran dos campos separados: nombre y
      // app/apellido). Se compara palabra por palabra para que funcione sin
      // importar el orden en que se escriban (ej. "Pérez Juan" o "Juan Pérez").
      const nombreCompleto = [data.nombre, data.nombre2, data.app, data.apm]
        .filter(Boolean).join(' ').toLowerCase();
      const alias = data.alias?.toLowerCase() || '';
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

      // Sección se filtra por la calle asignada en el catálogo (calleId ->
      // zonaId) O, si el usuario no tiene casa asignada, por el texto libre
      // de su dirección (calleTexto) contra los nombres de calle de esa
      // sección -- si no, los usuarios sin casa desaparecían del filtro.
      const matchSeccion = !searchTerms.seccionId ||
        String(this.zonaPorCalleId.get(data.calleId as number)) === searchTerms.seccionId ||
        this.todasLasCalles.some(c =>
          String(c.zonaId) === searchTerms.seccionId && calleTexto.includes(c.nombre.toLowerCase())
        );

      const palabrasNombre: string[] = searchTerms.nombre
        ? searchTerms.nombre.split(' ').filter((w: string) => w.length > 0)
        : [];
      const matchNombre = palabrasNombre.every((palabra: string) => nombreCompleto.includes(palabra));

      return matchNombre &&
            alias.includes(searchTerms.alias) &&
            noUser.includes(searchTerms.noUser) &&
            matchCalle &&
            matchCasa &&
            matchSeccion;
    };

    this.loadSeccionesYCalles();
    this.getUsers();
  }

  private loadSeccionesYCalles(): void {
    this.catalogService.getOptionsByClave('SECCIONES_COLONIA').subscribe({
      next: (opts) => this.secciones = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptions(15).subscribe({
      next: (opts) => {
        this.todasLasCalles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.calles = this.todasLasCalles;
        this.zonaPorCalleId = new Map(
          opts.filter(c => c.zonaId != null).map(c => [c.catalogoOpcionesId, c.zonaId as number])
        );
      },
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
      alias: this.aliasFiltro.trim().toLowerCase(),
      noUser: this.noUserFiltro.trim(),
      calleId: this.calleIdFiltro != null ? String(this.calleIdFiltro) : '',
      calleNombre: this.calleNombreFiltro.trim().toLowerCase(),
      casaNo: this.casaNoFiltro.trim(),
      seccionId: this.seccionIdFiltro != null ? String(this.seccionIdFiltro) : ''
    });
  }

  // Al elegir Sección se filtra el dropdown de Calle a solo las de esa
  // sección (y se limpia la calle elegida previamente), igual que en
  // house-list.
  applySeccionFiltro(seccionId: number | null): void {
    this.seccionIdFiltro = seccionId;
    this.calleIdFiltro = null;
    this.calleNombreFiltro = '';
    this.calles = seccionId != null
      ? this.todasLasCalles.filter(c => c.zonaId === seccionId)
      : this.todasLasCalles;
    this.aplicarFiltro();
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

  // Da de alta un usuario NUEVO copiando los datos personales y de domicilio
  // de uno ya existente (mismo caso: la misma familia agrega otra toma/cuenta
  // a nombre de alguien que ya está registrado en esa casa). Solo cambian
  // N° Usuario, Cuota y, si aplica, Observaciones -- eso se captura en blanco.
  copiarUsuario(element: WaterUserBasicModel): void {
    this.userService.getUserDetails(element.usuarioId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code !== '00') {
          this.openSnackBar('No se pudo cargar el usuario a copiar', 'Error');
          return;
        }
        const copyFrom = resp.data[0];
        const dialogRef = this.dialog.open(NewUserComponent, {
          width: '900px',
          data: { copyFrom }
        });
        dialogRef.afterClosed().subscribe((result: any) => {
          if (result == 1) {
            this.openSnackBar('Usuario agregado', 'Éxito');
            this.getUsers();
          } else if (result == 2) {
            this.openSnackBar('Error al guardar usuario', 'Error');
          }
        });
      },
      error: (e: any) => {
        console.error(e);
        this.openSnackBar('No se pudo cargar el usuario a copiar', 'Error');
      }
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



