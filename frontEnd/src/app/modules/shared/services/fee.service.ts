import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/fee";

@Injectable({
  providedIn: 'root'
})
export class FeeService {

  constructor(private http:HttpClient){}

  getFeeAmount(){
    const endpoint = `${base_url}/`;
    return this.http.get(endpoint);
  }
}
