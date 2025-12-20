package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.model.ExtractorJobResponse;
import com.rms.funds.holdings.analyser.model.ExtractorJobUpdateRequest;

import java.util.List;

public interface ExtractorJobService {

    List<ExtractorJobResponse> findAll();

    List<ExtractorJobResponse> upsert(ExtractorJobUpdateRequest updateRequest);
}
