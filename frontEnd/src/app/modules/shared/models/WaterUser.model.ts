import { AdressModel } from "./Adress.model";
import { CatalogOptionModel } from "./Catalog.model";
import { FeeAmountModel } from "./Fee.model";
import { PersonModel } from "./Person.model";

export interface WaterUserModel{
    aguaUsuarioId: number;
    id: number;
    fkIdCuota: number;
    fkFrecuenciaPagoId: number;
    fkEstatusTomaId: number;
    feeAmount:FeeAmountModel;
    estatusPago:CatalogOptionModel;
    estatusComite:CatalogOptionModel;
    estatusToma:CatalogOptionModel;
    frecuenciaPago:CatalogOptionModel;
    noUsuario : string;
    habitaDomicilio: boolean;
    tieneToma: boolean;
    inmuebleRenta: boolean;
    email: string;
    observaciones: string;
    casaNo: number;
    person : PersonModel;
    adress : AdressModel;
  }

  export interface WaterUserBasicModel{
    noUsuario: number;
    usuarioId: number;
    personaId: number;
    direccionId: number;
    casaId: number;
    nombre: string;
    app: string;
    apm: string;
    direccion: string;
    casa: string;
    calleId: number;
    casaNo: number;
    calleNombre: string;
  }

  export interface WaterUserDetailModel{
    noUsuario: number;
    usuarioId: number;
    aguaUsuarioId: number;
    personaId: number;
    direccionId: number;
    seccionId: number;
    casaId: number;
    nombre: string;
    app: string;
    apm: string;
    direccion: string;
    casaNombre: string;
    frecuenciaPagoId: number;
    estatusPagoId: number;
    estatusComiteId: number;
    estatusTomaId: number;
  }
  
  export interface UserAutocompleteModel {
    idUsuario: number;
    noUsuario: number;
    nombre: string;
  }

  export interface WaterHouseModel {
    casaId: number;
    calleId: number;
    casaNo: number;
    nombre: string;
    calle: string;
    lado: string;
    observaciones: string;
    lat: number; // coordenada en grados decimales
    lng: number; // coordenada en grados decimales
    listWaterUser: WaterUserModel[];
  }

   export interface WaterGroupModel {
    grupoId: number;
    nombre: string;
    observaciones: string;
    listWaterUser: [];
  }