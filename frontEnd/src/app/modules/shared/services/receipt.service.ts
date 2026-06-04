import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/waterReceipt";

@Injectable({
  providedIn: 'root'
})
export class ReceiptService {

  constructor(private http:HttpClient) { }

  getReceipt(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  saveReceipt(body:any){
    const endpoint = `${base_url}/`;
    return this.http.post(endpoint, body);
  }

  saveReceiptCancelled(body:any){
    const endpoint = `${base_url}/cancelled`;
    return this.http.post(endpoint, body);
  }

  getReceiptByNoFolioOrNoUser(no:any){
    const endpoint = `${base_url}/findByNoFolioOrNoUsuario/${no}`;
    return this.http.get(endpoint);
  }

  getReceiptByNoUser(noUser:any){
    const endpoint = `${base_url}/findByNoUsuario/${noUser}`;
    return this.http.get(endpoint);
  }

  exportReportReceipt(){
    const endpoint = `${base_url}/report`;
    return this.http.get(endpoint,{
      responseType:'blob'
    });
  }

}
