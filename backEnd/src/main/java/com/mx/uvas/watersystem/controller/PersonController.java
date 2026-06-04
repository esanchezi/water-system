package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.response.PersonRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IPersonService;
import com.mx.uvas.watersystem.services.IWaterUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/person" )
@AllArgsConstructor
public class PersonController {

    private final IPersonService personService;

    @GetMapping("/")
    public ResponseEntity<PersonRestResponse> searchUsers() {
        return personService.search();
    }

    @GetMapping("/findByIdPersona/{personaId}")
    public ResponseEntity<PersonRestResponse> findByIdPersona(@PathVariable Integer personaId) {
        return personService.findByIdPersona(personaId);
    }

    @Operation(summary = "Crea persona")
    @PostMapping(path = "/setPerson")
    public ResponseEntity<PersonDto> setPerson (@Valid @RequestBody PersonDto request){
        return ResponseEntity.ok(personService.create(request));
    }

    @Operation(summary = "Crea o actualiza persona y dirección")
    @PostMapping(path = "/savePersonAndAddress")
    public ResponseEntity<WaterUserDto> savePersonAndAddress(@Valid @RequestBody WaterUserDto request) {
        return ResponseEntity.ok(personService.savePersonAndAddress(request));
    }

}
