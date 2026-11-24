package validation;

import exception.InvalidInputException;
import model.VehicleCondition;

import java.time.Clock;
import java.time.Year;
import java.util.Locale;

public class LoanValidator {

    public static final long MAX_LOAN_AMOUNT = 1_000_000_000L;
    public static final int MIN_TENURE_YEARS = 1;
    public static final int MAX_TENURE_YEARS = 6;
    public static final int MIN_DP_PERCENT_NEW = 35;
    public static final int MIN_DP_PERCENT_USED = 25;

    private final Clock clock;

    public LoanValidator() {
        this(Clock.systemDefaultZone());
    }

    public LoanValidator(Clock clock) {
        this.clock = clock;
    }

    public int parseYear(String text, VehicleCondition condition) {
        String value = requireDigits(text, "Tahun kendaraan");
        if (value.length() != 4) {
            throw new InvalidInputException("Tahun kendaraan harus 4 digit angka, bukan: " + text);
        }
        int year = Integer.parseInt(value);
        int minYearForNew = Year.now(clock).getValue() - 1;
        if (condition == VehicleCondition.NEW && year < minYearForNew) {
            throw new InvalidInputException(
                    "Kendaraan baru tidak boleh lebih lama dari tahun " + minYearForNew);
        }
        return year;
    }

    public long parseLoanAmount(String text) {
        String value = requireDigits(text, "Jumlah pinjaman");
        long amount = toLong(value, "Jumlah pinjaman");
        if (amount <= 0) {
            throw new InvalidInputException("Jumlah pinjaman harus lebih dari 0");
        }
        if (amount > MAX_LOAN_AMOUNT) {
            throw new InvalidInputException("Jumlah pinjaman tidak boleh lebih dari 1 miliar (1.000.000.000)");
        }
        return amount;
    }

    public int parseTenure(String text) {
        String value = requireDigits(text, "Tenor pinjaman");
        long tenure = toLong(value, "Tenor pinjaman");
        if (tenure < MIN_TENURE_YEARS || tenure > MAX_TENURE_YEARS) {
            throw new InvalidInputException(
                    "Tenor pinjaman harus " + MIN_TENURE_YEARS + "-" + MAX_TENURE_YEARS + " tahun");
        }
        return (int) tenure;
    }

    public long parseDownPayment(String text, VehicleCondition condition, long loanAmount) {
        String value = requireDigits(text, "Jumlah DP");
        long dp = toLong(value, "Jumlah DP");
        if (dp >= loanAmount) {
            throw new InvalidInputException("Jumlah DP harus lebih kecil dari jumlah pinjaman");
        }
        int minPercent = condition == VehicleCondition.NEW ? MIN_DP_PERCENT_NEW : MIN_DP_PERCENT_USED;
        if (dp * 100 < loanAmount * minPercent) {
            long minAmount = (loanAmount * minPercent + 99) / 100;
            throw new InvalidInputException(String.format(Locale.US,
                    "DP kendaraan %s minimal %d%% dari jumlah pinjaman (Rp %,d)",
                    condition.getLabel().toLowerCase(), minPercent, minAmount));
        }
        return dp;
    }

     private String requireDigits(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException(fieldName + " tidak boleh kosong");
        }
        String value = text.trim();
        if (!value.matches("\\d+")) {
            throw new InvalidInputException(fieldName + " harus berupa angka, bukan: " + text);
        }
        return value;
    }

    private long toLong(String digits, String fieldName) {
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " terlalu besar: " + digits);
        }
    }
}