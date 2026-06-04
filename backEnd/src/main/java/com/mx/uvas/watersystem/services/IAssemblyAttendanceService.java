package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.AssemblyRestResponse;
import org.springframework.http.ResponseEntity;

public interface IAssemblyAttendanceService {
    ResponseEntity<AssemblyRestResponse> findByIdAssembly(Integer idAssembly);

}