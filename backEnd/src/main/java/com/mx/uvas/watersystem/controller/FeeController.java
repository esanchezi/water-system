package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.response.FeeRestResponse;
import com.mx.uvas.watersystem.services.IFeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/fee" )
@AllArgsConstructor
public class FeeController {

    private final IFeeService feeService;

    @GetMapping("/")
    public ResponseEntity<FeeRestResponse> searchAll() {
        return feeService.searchAll();
    }


}
