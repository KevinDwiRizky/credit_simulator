package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehicleTest {

    @Test
    void carHasBaseRate8Percent() {
        Vehicle car = new Car(VehicleCondition.NEW, 2026);
        assertEquals(0, new BigDecimal("8").compareTo(car.getBaseRatePercent()));
        assertEquals("Mobil", car.getTypeName());
    }

    @Test
    void motorHasBaseRate9Percent() {
        Vehicle motor = new Motor(VehicleCondition.USED, 2020);
        assertEquals(0, new BigDecimal("9").compareTo(motor.getBaseRatePercent()));
        assertEquals("Motor", motor.getTypeName());
    }

    @Test
    void principalIsLoanMinusDownPayment() {
        Vehicle car = new Car(VehicleCondition.NEW, 2026);
        LoanRequest req = new LoanRequest(car, 100_000_000L, 3, 25_000_000L);
        assertEquals(75_000_000L, req.principal());
    }
}
