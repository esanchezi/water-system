package com.mx.uvas.watersystem.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
// BaseRestResponse
public abstract class BaseRestResponse<T> {
    private List<T> data;
    private Map<String, Object> metadata = new HashMap<>();

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void addMetadata(String message, String code, String detail) {
        metadata.put("message", message);
        metadata.put("code", code);
        metadata.put("detail", detail);
    }
}