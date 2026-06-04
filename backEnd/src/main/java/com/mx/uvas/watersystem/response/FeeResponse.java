package com.mx.uvas.watersystem.response;

import com.mx.uvas.watersystem.dto.FeeDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FeeResponse implements Response<FeeDto>{

    private List<FeeDto> fee = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public List<FeeDto> getData() {
        return fee;
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
