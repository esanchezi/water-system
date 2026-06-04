package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.dto.AguaUsuarioSearchDTO;
import com.mx.uvas.watersystem.dto.WaterUserBasicDto;
import com.mx.uvas.watersystem.dto.WaterUserDetailsDto;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IWaterUserRepository extends JpaRepository<WaterUserEntity,Integer>{

    @Query("SELECT wu " +
            "FROM WaterUserEntity wu " +
            "WHERE wu.estatus = 1 AND wu.noUsuario =:noUser  " +
            "ORDER BY wu.noUsuario")
    WaterUserEntity findByNoUsuario(Integer noUser);

    @Query("SELECT wu " +
            "FROM WaterUserEntity wu " +
            "JOIN  wu.person p " +
            "WHERE wu.estatus = 1 AND p.personaId =:personaId " +
            "ORDER BY p.nombre")
    List<WaterUserEntity> findByPersonaId(Integer personaId);

    @Query("SELECT new com.mx.uvas.watersystem.dto.WaterUserBasicDto(" +
            "wu.noUsuario,wu.aguaUsuarioId, p.personaId,a.direccionId,0, " +
            "p.nombre, COALESCE(p.nombre2, ''), p.app, COALESCE(p.apm, ''), " +
            "CONCAT(a.calle, ' #', a.numero),h.nombre) " +
            "FROM  WaterUserEntity wu " +
            "JOIN  wu.person p " +
            "JOIN  wu.address a " +
            "LEFT JOIN wu.waterHouse h " +
            "WHERE wu.estatus = 1 " +
            "ORDER BY p.nombre")
    List<WaterUserBasicDto> getListUsers();

    @Query("SELECT new com.mx.uvas.watersystem.dto.WaterUserDetailsDto(" +
            "wu.aguaUsuarioId, wu.noUsuario, wu.habitaDomicilio, wu.tieneToma, wu.inmuebleRenta, wu.feeAmount.cuotaMontoId, " +
            "wu.frecuenciaPago.catalogoOpcionesId, wu.estatusPago.catalogoOpcionesId,wu.estatusComite.catalogoOpcionesId," +
            "wu.estatusToma.catalogoOpcionesId," +
            "p.personaId, p.nombre, COALESCE(p.nombre2,''), p.app, COALESCE(p.apm,''), " +
            "a.direccionId,a.catSeccion.catalogoOpcionesId, a.calle, a.numero, a.referencia, a.entrecalle1, a.entrecalle2, a.catSeccion.nombre, " +
            "h.nombre,h.casaId) " +
            "FROM WaterUserEntity wu " +
            "JOIN wu.person p " +
            "JOIN wu.address a " +
            "LEFT JOIN wu.waterHouse h " +
            "WHERE wu.aguaUsuarioId = :aguaUsuarioId")
    WaterUserDetailsDto getUserDetails(@Param("aguaUsuarioId") Integer aguaUsuarioId);

    List<WaterUserEntity> findAllByOrderByNoUsuario();

    @Query("SELECT wu " +
            "FROM WaterUserEntity wu " +
            "JOIN  wu.person p " +
            "WHERE wu.estatus = 1 AND (p.nombre LIKE %:noUser% or p.nombre2 LIKE %:noUser% or p.app LIKE %:noUser% or p.apm LIKE %:noUser%) " +
            "ORDER BY p.nombre")
    List<WaterUserEntity> findByNombre(String noUser);

    @Query("SELECT wu " +
            "FROM WaterUserEntity wu " +
            "JOIN  wu.address a " +
            "WHERE wu.estatus = 1 AND a.calle LIKE %:calle%  ")
    List<WaterUserEntity> findByStreet(String calle);

    @Query("SELECT wu " +
            "FROM WaterUserEntity wu " +
            "JOIN  wu.person p " +
            "WHERE wu.estatus = 1 AND (p.nombre LIKE %:name% or p.nombre2 LIKE %:name% ) and (p.app LIKE %:app% or p.apm LIKE %:app%) " +
            "ORDER BY p.nombre")
    List<WaterUserEntity> findByNombreApp(String name, String app);


    @Query(value = """
        SELECT 
            au.agua_usuario_id AS aguaUsuarioId,
            au.no_usuario AS noUsuario,
            CONCAT(
                p.nombre,' ',
                COALESCE(p.nombre2,''),' ',
                COALESCE(p.app,''),' ',
                COALESCE(p.apm,'')
            ) AS nombreCompleto
        FROM agua_usuario au
        INNER JOIN persona p ON p.persona_id = au.persona_id
        WHERE au.no_usuario LIKE %:term%
        AND au.estatus = 1
        LIMIT 10
    """, nativeQuery = true)
    List<AguaUsuarioSearchDTO> searchByNoUsuario(@Param("term") String term);

}
