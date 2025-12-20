package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.model.FilterAttributes;
import com.rms.funds.holdings.analyser.model.FilterCriteria;

public interface FilterAttributeService {

    FilterAttributes getAll(FilterCriteria filterCriteria);
}
