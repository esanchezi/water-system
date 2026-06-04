package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.response.FamilyMemberRestResponse;
import com.mx.uvas.watersystem.response.ProcessRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IFamilyMemberService;
import com.mx.uvas.watersystem.services.IProcessService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/family" )
@AllArgsConstructor
public class FamilyController {

    private final IFamilyMemberService familyMemberService;

    @GetMapping("/")
    public ResponseEntity<FamilyMemberRestResponse> search() {
        return familyMemberService.search();
    }

    @GetMapping("/findByIdPerson/{idPerson}")
    public ResponseEntity<FamilyMemberRestResponse> findByIdPerson(@PathVariable Integer idPerson) {
        return familyMemberService.findByIdPerson(idPerson);
    }

}
