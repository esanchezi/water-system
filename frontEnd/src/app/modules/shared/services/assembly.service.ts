import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AttendanceModel } from '../models/Assembly.model';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

const base_url = `${environment.apiUrl}/assembly`;

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
