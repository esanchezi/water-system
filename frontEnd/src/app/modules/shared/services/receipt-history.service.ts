
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

const base_url = `${environment.apiUrl}/waterReceiptHistory`;

@Injectable({
  providedIn: 'root'
})
export class ReceiptHistoryService {

  constructor(private http:HttpClient) { }

  getReceipt(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }

  getReceiptByNoFolioOrNoUser(no:any){
    const endpoint = `${base_url}/findByNoFolioOrNombre/${no}`;
    return this.http.get(endpoint);
  }

  saveReceiptHistory(body:any){
    const endpoint = `${base_url}/updateReceiptHistory`;
    return this.http.post(endpoint, body);
  }
}

