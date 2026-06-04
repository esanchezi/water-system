package com.mx.uvas.watersystem.response;

import com.mx.uvas.watersystem.dto.CatalogDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogResponse implements Response<CatalogDto>{

    private List<CatalogDto> catalog = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public List<CatalogDto> getData() {
        return catalog;
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
