package de0.coxvanguards;

public enum NumberLocationSetting {
    DEFAULT_CENTER,
    BELOW_VANGUARD,
    ABOVE_VANGUARD;

    @Override
    public String toString() {
        switch (this) {
            case BELOW_VANGUARD:
                return "Below";
            case ABOVE_VANGUARD:
                return "Above";
            case DEFAULT_CENTER:
            default:
                return "Center (Default)";
        }
    }
}
