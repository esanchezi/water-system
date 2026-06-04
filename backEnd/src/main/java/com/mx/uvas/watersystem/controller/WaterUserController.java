package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AguaUsuarioSearchDTO;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.response.WaterUserBasicRestResponse;
import com.mx.uvas.watersystem.response.WaterUserDetailsRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/waterUser" )
@AllArgsConstructor
public class WaterUserController {

    private final IWaterUserService waterUserService;

    @GetMapping("/")
    public ResponseEntity<WaterUserRestResponse> searchUsers() {
        return waterUserService.search();
    }

    @GetMapping("/getListUsers/")
    public ResponseEntity<WaterUserBasicRestResponse> getListUsers() {
        return waterUserService.getListUsers();
    }

    @GetMapping("/findByNoUser/{noUser}")
    public ResponseEntity<WaterUserRestResponse> findByNoUser(@PathVariable Integer noUser) {
        return waterUserService.findByNoUser(noUser);
    }

    @GetMapping("/search")
    public List<AguaUsuarioSearchDTO> searchUsers(@RequestParam String term) {
        return waterUserService.searchUsers(term);
    }

    @GetMapping("/details/{idUsuario}")
    public ResponseEntity<WaterUserDetailsRestResponse> getUserDetails(@PathVariable Integer idUsuario) {
        return waterUserService.findByIdUserDetails(idUsuario);
    }

    @GetMapping("/findByPersonaId/{personaId}")
    public ResponseEntity<WaterUserRestResponse> findByIdPersona(@PathVariable Integer personaId) {
        return waterUserService.findByPersonaId(personaId);
    }
    @GetMapping("/findByNombre/{nombre}")
    public ResponseEntity<WaterUserRestResponse> findByNombre(@PathVariable String nombre) {
        return waterUserService.findByNombre(nombre);
    }

    @GetMapping("/findByStreet/{calle}")
    public ResponseEntity<WaterUserRestResponse> findByStreet(@PathVariable String calle) {
        return waterUserService.findByStreet(calle);
    }

    @GetMapping("/findByNombreApp/{nombre}/{app}")
    public ResponseEntity<WaterUserRestResponse> findByNombreApp(@PathVariable String nombre,@PathVariable String app) {
        return waterUserService.findByNombreApp(nombre,app);
    }

    @Operation(summary = "Crea persona, direccion y usuario")
    @PostMapping(path = "/setUser")
    public ResponseEntity<WaterUserDto> setUser (@Valid @RequestBody WaterUserDto request){
        return ResponseEntity.ok(waterUserService.create(request));
    }

    @Operation(summary = "Modificar datos del usuario (excepto persona y dirección)")
    @PutMapping("/updateUserInfo")
    public ResponseEntity<Void> updateUserInfo(@Valid @RequestBody WaterUserDto request) {
        waterUserService.updateUserDataOnly(request);
        return ResponseEntity.ok().build();
    }

}
