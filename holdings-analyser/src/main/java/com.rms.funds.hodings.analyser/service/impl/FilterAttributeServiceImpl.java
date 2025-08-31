package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.model.FilterAttributes;
import com.rms.funds.hodings.analyser.model.FilterCriteria;
import com.rms.funds.hodings.analyser.repository.*;
import com.rms.funds.hodings.analyser.service.FilterAttributeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilterAttributeServiceImpl implements FilterAttributeService {

    private static FilterAttributes cachedFilterAttributes = null;

    private final SectorRepository sectorRepository;
    private final IndustryRepository industryRepository;
    private final MutualFundRepository mutualFundRepository;
    private final MutualFundTypeRepository mutualFundTypeRepository;
    private final StockInfoRepository stockInfoRepository;

    @Override
    public FilterAttributes getAll(FilterCriteria filterCriteria) {
        return FilterAttributes.builder()
                .sectors(getAttributes(cachedFilterAttributes.getSectors(), filterCriteria.getSectors(),
                        Collections.emptyMap()))
                .industry(getAttributes(cachedFilterAttributes.getIndustry(), filterCriteria.getIndustries(),
                        Map.of("sector", filterCriteria.getSectors())))
                .fundTypes(getAttributes(cachedFilterAttributes.getFundTypes(), filterCriteria.getFundTypes(),
                        Collections.emptyMap()))
                .fundNames(getAttributes(cachedFilterAttributes.getFundNames(), filterCriteria.getMfNames(),
                        Map.of("type", filterCriteria.getFundTypes())))
                .stockInfo(getAttributes(cachedFilterAttributes.getStockInfo(), filterCriteria.getStocks(),
                        Map.of("sector", filterCriteria.getSectors(), "industry", filterCriteria.getIndustries())))
                .build();
    }

    private List<FilterAttributes.Attribute> getAttributes(List<FilterAttributes.Attribute> attributes,
                                                           Set<String> selected,
                                                           Map<String, Set<String>> parentDepends) {
        return attributes.stream()
                .filter(x -> applyFilterCriteria(x, selected, parentDepends))
                .toList();
    }

    private boolean applyFilterCriteria(FilterAttributes.Attribute attribute,
                                        Set<String> selectedValues,
                                        Map<String, Set<String>> parentDepends) {
        boolean isAllow = CollectionUtils.isEmpty(selectedValues) || selectedValues.contains(attribute.getId().toString());
        if (!CollectionUtils.isEmpty(parentDepends)) {
            Set<String> parentDependsKeys = parentDepends.keySet();
            return attribute.getMetaInfo().keySet().containsAll(parentDependsKeys)
                    && parentDependsKeys.stream()
                    .allMatch(key -> CollectionUtils.isEmpty(parentDepends.get(key)) || parentDepends.get(key).contains(attribute.getMetaInfo().get(key)));
        }
        return isAllow;
    }

    @PostConstruct
    public void init() {
        cachedFilterAttributes = FilterAttributes.builder()
                .sectors(sectorRepository.findAll().stream().map(sector -> FilterAttributes.Attribute.builder()
                        .id(sector.getId().toString())
                        .name(sector.getName())
                        .description(sector.getDescription())
                        .build()).toList())
                .industry(industryRepository.findAll().stream().map(industry -> FilterAttributes.Attribute.builder()
                        .id(industry.getId().toString())
                        .name(industry.getName())
                        .description(industry.getDescription())
                        .metaInfo(Map.of("sector", industry.getSectorEntity().getId()+""))
                        .build()).toList())
                .fundTypes(mutualFundTypeRepository.findAll().stream().map(type -> FilterAttributes.Attribute.builder()
                        .id(type.getId().toString())
                        .name(type.getName())
                        .description(type.getDescription())
                        .build()).toList())
                .fundNames(mutualFundRepository.findAll().stream().map(fund -> FilterAttributes.Attribute.builder()
                        .id(fund.getId().toString())
                        .name(fund.getName())
                        .description(fund.getDescription())
                        .metaInfo(Map.of("type", fund.getFundTypeId()+""))
                        .build()).toList())
                .stockInfo(stockInfoRepository.findAll().stream().map(stock -> FilterAttributes.Attribute.builder()
                        .id(stock.getId().toString())
                        .name(stock.getCompany())
                        .description(stock.getSymbol())
                        .metaInfo(Map.of(
                                "sector", stock.getSectorEntity().getId()+"",
                                "industry", stock.getIndustry().getId()+"" )
                                )
                        .build()).toList())
                .build();
    }
}
