package service;

import model.Vehicle;

import java.math.BigDecimal;

public interface InterestPolicy {

    BigDecimal ratePercentForYear(Vehicle vehicle, int year);
}