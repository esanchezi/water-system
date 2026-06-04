package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.response.ProcessRestResponse;
import com.mx.uvas.watersystem.services.IProcessService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/process" )
@AllArgsConstructor
public class ProcessController {

    private final IProcessService processService;

    @GetMapping("/")
    public ResponseEntity<ProcessRestResponse> searchProcess() {
        return processService.search();
    }

    @GetMapping("/findByIdPerson/{idPerson}")
    public ResponseEntity<ProcessRestResponse> findByIdPerson(@PathVariable Integer idPerson) {
        return processService.findByIdPerson(idPerson);
    }
}
