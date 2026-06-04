package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.helpers.WaterUserHelper;
import com.mx.uvas.watersystem.mapping.WaterUserMapper;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IFeeAmountRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterUserServiceTest {

    @Mock
    private IFeeAmountRepository feeAmountRepository;

    @Mock
    private ICatalogOptionsRepository catalogOptionsRepository;

    @Mock
    private IWaterUserRepository waterUserRepository;

    @Mock
    private WaterUserHelper waterUserHelper;

    @Mock
    private WaterUserMapper waterUserMapper;

    @InjectMocks
    private WaterUserService waterUserService;

   /* @BeforeEach
    void setUp(){

    }*/

   // @Test
    void search() {
        // Configurar el comportamiento del mock del mapper
        when(waterUserMapper.entityToDto(any(WaterUserEntity.class))).thenAnswer(invocation -> {
            WaterUserEntity userEntity = invocation.getArgument(0);
            WaterUserDto userDto = new WaterUserDto();
            userDto.setNoUsuario( userEntity.getNoUsuario());
            userDto.setAguaUsuarioId(userEntity.getAguaUsuarioId());
            return userDto;
            /*return new WaterUserDto(
                   ,
                    userEntity.getHabitaDomicilio(),
                    userEntity.getTieneToma(),
                    "emailSimulado@example.com",
                    "Observaciones simuladas",
                    userEntity.getEstatus(),
                    userEntity.getUserIdAdd()
            );*/
        });

        when(waterUserRepository.findAll()).thenReturn(Datos.simulatedUsers);
        ResponseEntity<WaterUserRestResponse> list = waterUserService.search();
        System.out.println(list.getBody().getData());
        assertNotNull(list);
    }

    //@Test
    void findByNoUser() {
        when(waterUserMapper.entityToDto(any(WaterUserEntity.class))).thenAnswer(invocation -> {
            WaterUserEntity userEntity = invocation.getArgument(0);
            WaterUserDto userDto = new WaterUserDto();
            userDto.setNoUsuario( userEntity.getNoUsuario());
            userDto.setAguaUsuarioId(userEntity.getAguaUsuarioId());
            return userDto;
        });
        when(waterUserRepository.findByNoUsuario(1001)).thenReturn(Datos.user1);
        ResponseEntity<WaterUserRestResponse> responseEntity = waterUserService.findByNoUser(1001);
        WaterUserRestResponse waterUserRestResponse = responseEntity.getBody();

        assertNotNull(responseEntity);
        List<WaterUserDto> waterUserDtos = waterUserRestResponse.getData();
        if (!waterUserDtos.isEmpty()) {
            WaterUserDto waterUserDto = waterUserDtos.get(0);
            Integer noUsuario = waterUserDto.getNoUsuario();
            String email = waterUserDto.getEmail();

            assertEquals(1001, noUsuario);
        } else {
            fail("No se encontraron usuarios con el número de usuario 1001");
        }
        //verify(waterUserRepository).findAll();
    }

   /* @Test
    void findByNombre() {
    }

    @Test
    void create() {
    }*/
}