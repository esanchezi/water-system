package com.mx.uvas.watersystem.response;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WaterUserNoticeResponse implements Response<WaterUserNoticeDto>{

    private List<WaterUserNoticeDto> notice = new ArrayList<>();
    private List<Metadata> metadata = new ArrayList<>();

    @Override
    public List<WaterUserNoticeDto> getData() {
        return notice;
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
