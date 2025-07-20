package com.rms.funds.hodings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DownloadLinkCreateRequest {

    private Map<Long, Map<String, String>> downloadLinks;

}
