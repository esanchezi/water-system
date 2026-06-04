package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.AssemblyAttendanceEntity;
import com.mx.uvas.watersystem.model.AssemblyEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AssemblyMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("es", "MX"));

    public AssemblyDto entityToDto(AssemblyEntity entity) {
        var response = new AssemblyDto();
        BeanUtils.copyProperties(entity,response);
        if (entity.getDate() != null) {
            response.setDateS(entity.getDate().format(formatter));
        }

        if (entity.getAssemblyAttendanceEntity() != null) {
            var attendanceDtos = entity.getAssemblyAttendanceEntity()
                    .stream()
                    .map(this::attendanceEntityToDto)
                    .toList(); // Java 16+ tiene .toList() directo
            response.setAttendance(attendanceDtos);
        }
        return response;
    }

    public AssemblyAttendanceDto attendanceEntityToDto(AssemblyAttendanceEntity entity) {
        var response = new AssemblyAttendanceDto();
        BeanUtils.copyProperties(entity,response);
        var user = new WaterUserDto();
        if(entity.getWaterUser() != null){
            BeanUtils.copyProperties(entity.getWaterUser(),user);
            response.setWaterUser(user);
            if (entity.getWaterUser().getPerson() != null){
                var person = new PersonDto();
                BeanUtils.copyProperties(entity.getWaterUser().getPerson(),person);
                response.getWaterUser().setPerson(person);
            }else{
                response.getWaterUser().setPerson(new PersonDto());
            }
        }else{
            response.setWaterUser(new WaterUserDto());
        }
        return response;
    }
}
