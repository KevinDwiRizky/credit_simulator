package service;

import model.Vehicle;

import java.math.BigDecimal;

public class StepUpInterestPolicy implements InterestPolicy {

    private static final BigDecimal EVEN_YEAR_STEP = new BigDecimal("0.1");
    private static final BigDecimal ODD_YEAR_STEP = new BigDecimal("0.5");

    @Override
    public BigDecimal ratePercentForYear(Vehicle vehicle, int year) {
        if (year < 1) {
            throw new IllegalArgumentException("Tahun cicilan minimal 1, bukan: " + year);
        }
        BigDecimal rate = vehicle.getBaseRatePercent();
        for (int y = 2; y <= year; y++) {
            rate = rate.add(y % 2 == 0 ? EVEN_YEAR_STEP : ODD_YEAR_STEP);
        }
        return rate;
    }
}
