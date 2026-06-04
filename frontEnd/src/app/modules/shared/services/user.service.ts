import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { UserAutocompleteModel, WaterUserBasicModel, WaterUserModel } from '../models/WaterUser.model';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/waterUser";
//const base_url= "http://springboot-app-412420.wl.r.appspot.com/Los_Lopez/api/v1/waterUser";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http:HttpClient) {}

  getUsers(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  searchUsersByNumber(term: string) {
  return this.http.get<any>(`${base_url}/search?term=${term}`);
}

  getBasicUsers(){
    const endpoint = `${base_url}/getListUsers/`;
    return this.http.get(endpoint);
  }

  getUserDetails(noUsuario: number): Observable<WaterUserBasicModel> {
    return this.http.get<WaterUserBasicModel>(`${base_url}/details/${noUsuario}`);
  }

  getUsersByNoUser(noUser:any){
    const endpoint = `${base_url}/findByNoUser/${noUser}`;
    return this.http.get(endpoint);
  }

  getUsersByName(nombre:any){
    const endpoint = `${base_url}/findByNombre/${nombre}`;
    return this.http.get(endpoint);
  }

  getUsersByNameT(nombre: string): Observable<UserAutocompleteModel[]> {
    const endpoint = `${base_url}/findByNombre/${nombre}`;
    return this.http.get<{data: any[]}>(endpoint).pipe(
      map(response => response.data.map(user => ({
        idUsuario: user.aguaUsuarioId,
        noUsuario: user.noUsuario,
        nombre: `${user.person.nombre} ${user.person.nombre2 || ''} ${user.person.app} ${user.person.apm}`.trim()
      })))
    );
  }

  getUsersByStreet(calle:any){
    const endpoint = `${base_url}/findByStreet/${calle}`;
    return this.http.get(endpoint);
  }

  getUsersByNameApp(nombre:any, app:any){
    const endpoint = `${base_url}/findByNombreApp/${nombre}/${app}`;
    console.log(endpoint);
    return this.http.get(endpoint);
  }

  getuserCountByStatus(){
    const endpoint = `${base_url}/userCountByStatus`;
    return this.http.get(endpoint);
  }

  getuserMoraCountByZona(){
    const endpoint = `${base_url}/userMoraCountByZona`;
    return this.http.get(endpoint);
  }

  saveUser(body:any){
    const endpoint = `${base_url}/setUser`;
    return this.http.post(endpoint, body);
  }

  saveWaterUser(body:any){
    const endpoint = `${base_url}/updateUserInfo`;
    return this.http.put(endpoint, body);
  }

  getUserByName(name:any){
    const endpoint = `${base_url}/users/${name}`;
    return this.http.get(endpoint);
  }

  getUsersByPersonId(idPersona: number): Observable<WaterUserModel[]> {
    return this.http.get<WaterUserModel[]>(`${base_url}/findByPersonaId/${idPersona}`);
  }
}
