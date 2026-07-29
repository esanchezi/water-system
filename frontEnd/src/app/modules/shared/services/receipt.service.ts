import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

const base_url = `${environment.apiUrl}/waterReceipt`;

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

  updateReceipt(id: number, body: any){
    const endpoint = `${base_url}/${id}`;
    return this.http.put(endpoint, body);
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
