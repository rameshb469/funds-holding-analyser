package com.rms.funds.holdings.analyser.model;

import lombok.Getter;

@Getter
public enum MarketCapCategoryType {
    LARGE_CAP ("Large Cap"),
    MID_CAP("Mid Cap"),
    SMALL_CAP("Small Cap"),
    MIRCO_CAP("Micro Cap");

    private final String displayName;

    MarketCapCategoryType(String displayName) {
        this.displayName = displayName;
    }

    public static MarketCapCategoryType fromDisplayName(String displayName) {
        for (MarketCapCategoryType type : MarketCapCategoryType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No MarketCapCategoryType with display name " + displayName + " found.");
    }

    public static MarketCapCategoryType fromId(String id) {
        for (MarketCapCategoryType type : MarketCapCategoryType.values()) {
            if (type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

}
