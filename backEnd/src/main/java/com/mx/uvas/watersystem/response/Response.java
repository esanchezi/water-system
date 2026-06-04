package com.mx.uvas.watersystem.response;

import java.util.List;

public interface Response<T> {
    List<T> getData();
    List<Metadata> getMetadata();
    void addMetadata(String type, String code, String date);
}
