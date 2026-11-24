package view;

import service.YearlyInstallment;

import java.math.BigDecimal;
import java.util.Locale;

public class InstallmentFormatter {

    public String formatMoney(BigDecimal amount) {
        return String.format(Locale.US, "Rp. %,.2f", amount);
    }

    public String formatRate(BigDecimal ratePercent) {
        return ratePercent.stripTrailingZeros().toPlainString().replace('.', ',') + "%";
    }

    public String formatLine(YearlyInstallment item) {
        return "tahun " + item.year()
                + " : " + formatMoney(item.monthlyInstallment())
                + "/bln , Suku Bunga : " + formatRate(item.ratePercent());
    }
}