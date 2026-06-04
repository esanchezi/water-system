import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PersonModel } from '../models/Person.model';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/person";
//const base_url= "http://springboot-app-412420.wl.r.appspot.com/Los_Lopez/api/v1/waterUser";

@Injectable({
  providedIn: 'root'
})
export class PersonService {

  constructor(private readonly http:HttpClient) {}

  getPersons(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  getPersonByPersonId(personaId: any): Observable<PersonModel> {
    const endpoint = `${base_url}/findByIdPersona/${personaId}`;
    return this.http.get<any>(endpoint).pipe(
      map(resp => resp.data[0] as PersonModel)
    );
  }


  savePersonData(body:any){
    const endpoint = `${base_url}/setPerson`;
    return this.http.post(endpoint, body);
  }

  savePersonAndAddress(body:any){
    const endpoint = `${base_url}/savePersonAndAddress`;
    return this.http.post(endpoint, body);
  }
}
