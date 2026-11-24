package model;

import java.math.BigDecimal;

public class Motor extends Vehicle {

    public Motor(VehicleCondition condition, int year) {
        super(condition, year);
    }

    @Override
    public String getTypeName() {
        return "Motor";
    }

    @Override
    public BigDecimal getBaseRatePercent() {
        return new BigDecimal("9");
    }
}
