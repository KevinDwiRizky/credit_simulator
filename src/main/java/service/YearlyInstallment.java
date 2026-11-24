package service;

import java.math.BigDecimal;

public record YearlyInstallment(int year,
                                BigDecimal ratePercent,
                                BigDecimal monthlyInstallment) {
}
