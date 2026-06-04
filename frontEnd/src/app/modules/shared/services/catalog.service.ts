import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CatalogResponse } from '../models/Catalog.model';

const base_url= "http://localhost:8080/Los_Lopez/api/v1/catalog";

@Injectable({
  providedIn: 'root'
})
export class CatalogService {

  constructor(private http:HttpClient){}

  getCatalogById(id:any){
    const endpoint = `${base_url}/${id}`;
    return this.http.get(endpoint);
  }

  getCatalogByIdResponse(id:any){
    const endpoint = `${base_url}/${id}`;
    return this.http.get<CatalogResponse>(endpoint);
  }


}
