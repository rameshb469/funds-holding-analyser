package com.rms.funds.hodings.analyser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractorJobUpdateRequest {

    @Builder.Default
    private Map<Long, String> updatedLinks = new LinkedHashMap<>();
}
