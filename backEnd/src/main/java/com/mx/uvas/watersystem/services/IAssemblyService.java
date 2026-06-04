package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.AssemblyAttendanceDto;
import com.mx.uvas.watersystem.response.AssemblyRestResponse;
import org.springframework.http.ResponseEntity;

public interface IAssemblyService {

    ResponseEntity<AssemblyRestResponse> findAll();
    ResponseEntity<AssemblyRestResponse> findByNoUsuario(Integer noUser);
    AssemblyAttendanceDto create(AssemblyAttendanceDto request);

}