import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AttendanceModel } from '../models/Assembly.model';
import { Observable } from 'rxjs';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/assembly";
//const base_url= "http://springboot-app-412420.wl.r.appspot.com/Los_Lopez/api/v1/waterUser";

@Injectable({
  providedIn: 'root'
})
export class AssemblyService {

  constructor(private http:HttpClient) {}

  getAssembly(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  getAssemblyById(idAssembly:any) : Observable<AttendanceModel[]> {
    return this.http.get<AttendanceModel[]>(`${base_url}/findByIdAssembly/${idAssembly}`);
  }

    //const endpoint = `${base_url}/`;
    //return this.http.get(endpoint);

  getAssemblyByNoUser(noUser:any){
    const endpoint = `${base_url}/findByNoUsuario/${noUser}`;
    return this.http.get(endpoint);
  }

  saveAttendance(body:any){
    const endpoint = `${base_url}/`;
    return this.http.post(endpoint, body);
  }

}
