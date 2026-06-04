package com.mx.uvas.watersystem.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RestResponse<T> implements Response<T> {
    private List<T> data = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public void addMetadata(String type, String code, String date) {
        Metadata resp = new Metadata(type, code, date);
        this.metadata.add(resp);
    }
}
