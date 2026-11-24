package service;

import model.LoanRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int MONTHS_PER_YEAR = 12;
    private static final int INTERNAL_SCALE = 10;
    private static final int MONEY_SCALE = 2;

    private final InterestPolicy interestPolicy;

    public LoanCalculator(InterestPolicy interestPolicy) {
        this.interestPolicy = interestPolicy;
    }

    public List<YearlyInstallment> calculate(LoanRequest request) {
        List<YearlyInstallment> result = new ArrayList<>();
        BigDecimal principal = BigDecimal.valueOf(request.principal());
        int tenure = request.tenureYears();

        for (int year = 1; year <= tenure; year++) {
            BigDecimal rate = interestPolicy.ratePercentForYear(request.vehicle(), year);

            BigDecimal interestFactor = BigDecimal.ONE.add(rate.divide(HUNDRED));
            BigDecimal total = principal.multiply(interestFactor);

            int remainingMonths = MONTHS_PER_YEAR * (tenure - (year - 1));
            BigDecimal monthly = total.divide(
                    BigDecimal.valueOf(remainingMonths), INTERNAL_SCALE, RoundingMode.HALF_UP);

            result.add(new YearlyInstallment(
                    year, rate, monthly.setScale(MONEY_SCALE, RoundingMode.HALF_UP)));

            BigDecimal yearly = monthly.multiply(BigDecimal.valueOf(MONTHS_PER_YEAR));
            principal = total.subtract(yearly);
        }
        return result;
    }
}
