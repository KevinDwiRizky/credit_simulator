package model;

public record LoanRequest(Vehicle vehicle,
                          long totalLoanAmount,
                          int tenureYears,
                          long downPayment) {

    public long principal() {
        return totalLoanAmount - downPayment;
    }
}
