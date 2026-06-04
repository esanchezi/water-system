package com.mx.uvas.watersystem.response;

import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PeopleResponse implements Response<PersonDto>{

    private List<PersonDto> users = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public List<PersonDto> getData() {
        return users;
    }

    @Override
    public List<Metadata> getMetadata() {
        return metadata;
    }

    @Override
    public void addMetadata(String type, String code, String date) {
        Metadata metadata = new Metadata(type, code, date);
        this.metadata.add(metadata);
    }
}
