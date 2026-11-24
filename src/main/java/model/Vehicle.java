package model;

import java.math.BigDecimal;

public abstract class Vehicle {
    private final VehicleCondition condition;
    private final int year;

    protected Vehicle(VehicleCondition condition, int year) {
        this.condition = condition;
        this.year = year;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public int getYear() {
        return year;
    }

    public boolean isNew() {
        return condition == VehicleCondition.NEW;
    }

    public abstract String getTypeName();

    public abstract BigDecimal getBaseRatePercent();
}
