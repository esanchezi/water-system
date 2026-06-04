package com.mx.uvas.watersystem.response;

import com.mx.uvas.watersystem.dto.WaterReceiptHistoryDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WaterReceiptHistoryResponse implements Response<WaterReceiptHistoryDto>{

    private List<WaterReceiptHistoryDto> receipt = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public List<WaterReceiptHistoryDto> getData() {
        return receipt;
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
