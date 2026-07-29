import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

const base_url = `${environment.apiUrl}/waterUserNotice`;

@Injectable({
  providedIn: 'root'
})
export class UserNoticeService {

  constructor(private http:HttpClient) {}

  getUsersNotice(noUser:any){
    const endpoint = `${base_url}/${noUser}`;
    return this.http.get(endpoint);
  }

  saveNotice(body:any){
    const endpoint = `${base_url}/`;
    return this.http.post(endpoint, body);
  }
}
