package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.AssemblyAttendanceDto;
import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.response.AssemblyRestResponse;
import com.mx.uvas.watersystem.services.IAssemblyAttendanceService;
import com.mx.uvas.watersystem.services.IAssemblyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/assembly" )
@AllArgsConstructor
public class AssemblyController {

    private final IAssemblyService assemblyService;

    private final IAssemblyAttendanceService assemblyAttendanceService;

    @GetMapping("/")
    public ResponseEntity<AssemblyRestResponse> getAssembly() {
        return assemblyService.findAll();
    }

    @GetMapping("/findByIdAssembly/{idAssembly}")
    public ResponseEntity<AssemblyRestResponse> findByIdAssembly(@PathVariable Integer idAssembly) {
        return assemblyAttendanceService.findByIdAssembly(idAssembly);
    }

    @PostMapping("/")
    public ResponseEntity<AssemblyAttendanceDto> post (@RequestBody AssemblyAttendanceDto request){
        return ResponseEntity.ok(assemblyService.create(request));
    }

    @GetMapping("/findByNoUsuario/{noUser}")
    public ResponseEntity<AssemblyRestResponse> findByNoUsuario(@PathVariable Integer noUser) {
        return assemblyService.findByNoUsuario(noUser);
    }

}
