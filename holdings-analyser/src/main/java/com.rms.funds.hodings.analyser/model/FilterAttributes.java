package com.rms.funds.hodings.analyser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FilterAttributes {

    private List<Attribute> sectors;
    private List<Attribute> industry;
    private List<Attribute> fundTypes;
    private List<Attribute> fundHouses;
    private List<Attribute> fundNames;
    private List<Attribute> stockInfo;


    @Builder(toBuilder = true)
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {

        private String id;
        private String name;
        private String description;
        private Map<String, String> metaInfo;
    }
}
