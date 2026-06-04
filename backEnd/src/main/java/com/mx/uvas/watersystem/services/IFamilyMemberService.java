package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.FamilyMemberRestResponse;
import org.springframework.http.ResponseEntity;

public interface IFamilyMemberService {
    ResponseEntity<FamilyMemberRestResponse> search();
    ResponseEntity<FamilyMemberRestResponse> findByIdPerson(Integer personId);
}
