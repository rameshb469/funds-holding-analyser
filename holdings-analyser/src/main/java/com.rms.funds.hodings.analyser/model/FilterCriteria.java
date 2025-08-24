package com.rms.funds.hodings.analyser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FilterCriteria {

    @Builder.Default
    private Set<String> sectors = new HashSet<>();
    @Builder.Default
    private Set<String> industries = new HashSet<>();
    @Builder.Default
    private Set<String> fundTypes = new HashSet<>();
    @Builder.Default
    private Set<String> stocks = new HashSet<>();
    @Builder.Default
    private Set<String> symbols = new HashSet<>();
    @Builder.Default
    private Set<String> mfNames = new HashSet<>();
}
