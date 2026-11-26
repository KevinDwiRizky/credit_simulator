package controller;

import client.LoanDataSource;
import exception.InvalidInputException;
import factory.VehicleFactory;
import model.LoanRequest;
import model.Vehicle;
import model.VehicleCondition;
import service.Calculation;
import service.LoanCalculator;
import service.YearlyInstallment;
import validation.LoanValidator;
import view.ConsoleView;

import java.util.List;
import java.util.function.Function;

public class CreditSimulatorController {

    private final ConsoleView view;
    private final LoanValidator validator;
    private final LoanCalculator calculator;
    private final boolean retryOnInvalid;

    public CreditSimulatorController(ConsoleView view,
                                     LoanValidator validator,
                                     LoanCalculator calculator,
                                     boolean retryOnInvalid) {
        this.view = view;
        this.validator = validator;
        this.calculator = calculator;
        this.retryOnInvalid = retryOnInvalid;
    }

    public Calculation run() {
        return calculateAndShow(collectRequest());
    }

    public Calculation loadAndCalculate(LoanDataSource source) {
        LoanRequest request = source.load();
        view.showRequest(request);
        return calculateAndShow(request);
    }

    private Calculation calculateAndShow(LoanRequest request) {
        List<YearlyInstallment> result = calculator.calculate(request);
        view.showResult(result);
        return new Calculation(request, result);
    }

    private LoanRequest collectRequest() {
        String type = askUntilValid("Jenis kendaraan (Motor|Mobil)", VehicleFactory::normalizeType);
        VehicleCondition condition = askUntilValid("Kondisi kendaraan (Bekas|Baru)", VehicleCondition::fromText);
        int year = askUntilValid("Tahun kendaraan (4 digit)",
                text -> validator.parseYear(text, condition));
        long loan = askUntilValid("Jumlah pinjaman total (maks 1 miliar)", validator::parseLoanAmount);
        int tenure = askUntilValid("Tenor pinjaman (1-6 tahun)", validator::parseTenure);
        long downPayment = askUntilValid("Jumlah DP",
                text -> validator.parseDownPayment(text, condition, loan));

        Vehicle vehicle = VehicleFactory.create(type, condition.getLabel(), year);
        return new LoanRequest(vehicle, loan, tenure, downPayment);
    }

    private <T> T askUntilValid(String prompt, Function<String, T> parser) {
        while (true) {
            String answer = view.ask(prompt);
            try {
                return parser.apply(answer);
            } catch (InvalidInputException e) {
                if (!retryOnInvalid) {
                    throw e;
                }
                view.showError(e.getMessage());
            }
        }
    }
}