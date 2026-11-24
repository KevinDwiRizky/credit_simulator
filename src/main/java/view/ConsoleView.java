package view;

import exception.InputEndedException;
import service.YearlyInstallment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

    public void showResult(List<YearlyInstallment> installments) {
        out.println();
        out.println("Hasil perhitungan:");
        for (YearlyInstallment item : installments) {
            out.println(formatter.formatLine(item));
        }
    }
}