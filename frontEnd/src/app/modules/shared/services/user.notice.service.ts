import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/waterUserNotice";

@Injectable({
  providedIn: 'root'
})
export class UserNoticeService {

  constructor(private http:HttpClient) {}

  getUsersNotice(noUser:any){
    const endpoint = `${base_url}/${noUser}`;
    return this.http.get(endpoint);
  }
}
