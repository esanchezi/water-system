import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/family";
//const base_url= "http://springboot-app-412420.wl.r.appspot.com/Los_Lopez/api/v1/waterUser";

@Injectable({
  providedIn: 'root'
})
export class FamilyMemberService {

  constructor(private readonly http:HttpClient) {}

  getFamily(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  findByIdPerson(IdPerson:any){
    const endpoint = `${base_url}/findByIdPerson/${IdPerson}`;
    return this.http.get(endpoint);
  }
}
