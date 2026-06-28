package com.rms.funds.holdings.analyser.model;

import lombok.Getter;

@Getter
public enum MarketCapCategoryType {
    LARGE_CAP ("Large Cap"),
    MID_CAP("Mid Cap"),
    SMALL_CAP("Small Cap"),
    MICRO_CAP("Micro Cap");

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

    /**
     * Tolerate the original "MIRCO_CAP" spelling when reading legacy rows
     * (the V54 column has been written with both forms by different writers).
     */
    public static MarketCapCategoryType fromIdLenient(String id) {
        if (id == null) {
            return null;
        }
        MarketCapCategoryType direct = fromId(id);
        if (direct != null) {
            return direct;
        }
        if ("MIRCO_CAP".equalsIgnoreCase(id)) {
            return MICRO_CAP;
        }
        return null;
    }

    public boolean isLargeCap() {
        return this == LARGE_CAP;
    }
}
