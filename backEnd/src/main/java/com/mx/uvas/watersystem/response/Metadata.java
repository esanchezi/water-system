package com.mx.uvas.watersystem.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Metadata {

    private String type;
    private String code;
    private String date;

    public Metadata(String type, String code, String date) {
        this.type = type;
        this.code = code;
        this.date = date;
    }
}
