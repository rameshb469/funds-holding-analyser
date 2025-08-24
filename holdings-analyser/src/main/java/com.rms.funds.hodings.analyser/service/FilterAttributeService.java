package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.model.FilterAttributes;
import com.rms.funds.hodings.analyser.model.FilterCriteria;

public interface FilterAttributeService {

    FilterAttributes getAll(FilterCriteria filterCriteria);
}
