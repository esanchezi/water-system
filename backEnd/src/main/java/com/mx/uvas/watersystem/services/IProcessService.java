package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.ProcessRestResponse;
import org.springframework.http.ResponseEntity;

public interface IProcessService {
    ResponseEntity<ProcessRestResponse> search();

    ResponseEntity<ProcessRestResponse> findByIdPerson(Integer personId);
}
