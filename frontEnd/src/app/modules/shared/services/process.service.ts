import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

const base_url = `${environment.apiUrl}/process`;

@Injectable({
  providedIn: 'root'
})
export class ProcessService {

  constructor(private readonly http:HttpClient) {}

  getProcess(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  findByIdPerson(IdPerson:any){
    const endpoint = `${base_url}/findByIdPerson/${IdPerson}`;
    return this.http.get(endpoint);
  }
}
