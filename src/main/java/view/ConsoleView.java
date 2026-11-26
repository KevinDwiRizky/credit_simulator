package view;

import exception.InputEndedException;
import model.LoanRequest;
import service.Calculation;
import service.YearlyInstallment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

public class ConsoleView {

    private final BufferedReader in;
    private final PrintStream out;
    private final InstallmentFormatter formatter = new InstallmentFormatter();
    private final boolean echoInput;

    public ConsoleView() {
        this(new BufferedReader(new InputStreamReader(System.in)), System.out, false);
    }

    public ConsoleView(BufferedReader in, PrintStream out) {
        this(in, out, false);
    }

    public ConsoleView(BufferedReader in, PrintStream out, boolean echoInput) {
        this.in = in;
        this.out = out;
        this.echoInput = echoInput;
    }

    public String ask(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                if (echoInput) {
                    out.println();
                }
                throw new InputEndedException("Input berakhir sebelum semua data terisi");
            }
            if (echoInput) {
                out.println(line);
            }
            return line;
        } catch (IOException e) {
            throw new InputEndedException("Gagal membaca input: " + e.getMessage());
        }
    }

    public void showMessage(String message) {
        out.println(message);
    }

    public void showError(String message) {
        out.println("Error: " + message);
    }

    public void showRequest(LoanRequest request) {
        out.println("Data pinjaman yang dimuat:");
        printRequestFields(request);
    }

    public void showSheet(String name, Calculation calculation) {
        out.println("Sheet '" + name + "':");
        printRequestFields(calculation.request());
        showResult(calculation.installments());
    }

    private void printRequestFields(LoanRequest request) {
        printField("Jenis kendaraan", request.vehicle().getTypeName());
        printField("Kondisi", request.vehicle().getCondition().getLabel());
        printField("Tahun", String.valueOf(request.vehicle().getYear()));
        printField("Jumlah pinjaman", formatter.formatMoney(BigDecimal.valueOf(request.totalLoanAmount())));
        printField("Tenor", request.tenureYears() + " tahun");
        printField("DP", formatter.formatMoney(BigDecimal.valueOf(request.downPayment())));
    }

    private void printField(String label, String value) {
        out.println(String.format("%-16s : %s", label, value));
    }

    public void showResult(List<YearlyInstallment> installments) {
        out.println();
        out.println("Hasil perhitungan:");
        for (YearlyInstallment item : installments) {
            out.println(formatter.formatLine(item));
        }
    }
}