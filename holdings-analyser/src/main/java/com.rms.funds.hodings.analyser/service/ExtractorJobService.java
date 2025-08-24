package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.model.ExtractorJobResponse;
import com.rms.funds.hodings.analyser.model.ExtractorJobUpdateRequest;

import java.util.List;

public interface ExtractorJobService {

    List<ExtractorJobResponse> findAll();

    List<ExtractorJobResponse> upsert(ExtractorJobUpdateRequest updateRequest);
}
