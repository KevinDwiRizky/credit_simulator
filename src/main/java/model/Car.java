package model;

import java.math.BigDecimal;

public class Car extends Vehicle {

    public Car(VehicleCondition condition, int year) {
        super(condition, year);
    }

    @Override
    public String getTypeName() {
        return "Mobil";
    }

    @Override
    public BigDecimal getBaseRatePercent() {
        return new BigDecimal("8");
    }
}
