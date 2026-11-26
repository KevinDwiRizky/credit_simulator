package client;

import exception.InvalidInputException;
import exception.LoanLoadException;
import factory.VehicleFactory;
import json.JsonParseException;
import json.JsonParser;
import model.LoanRequest;
import model.Vehicle;
import model.VehicleCondition;
import validation.LoanValidator;

import java.math.BigDecimal;
import java.util.Map;

public class LoanRequestMapper {

    private static final int MAX_INTEGER_DIGITS = 18;

    private final LoanValidator validator;

    public LoanRequestMapper(LoanValidator validator) {
        this.validator = validator;
    }

    public LoanRequest map(String json) {
        Map<String, Object> data;
        try {
            data = JsonParser.parseObject(json);
        } catch (JsonParseException e) {
            throw new LoanLoadException("Response web service bukan JSON yang valid: " + e.getMessage());
        }

        try {
            String type = VehicleFactory.normalizeType(text(data, "vehicleType"));
            VehicleCondition condition = VehicleCondition.fromText(text(data, "vehicleCondition"));
            int year = validator.parseYear(text(data, "vehicleYear"), condition);
            long loan = validator.parseLoanAmount(text(data, "totalLoanAmount"));
            int tenure = validator.parseTenure(text(data, "loanTenure"));
            long downPayment = validator.parseDownPayment(text(data, "downPayment"), condition, loan);

            Vehicle vehicle = VehicleFactory.create(type, condition.getLabel(), year);
            return new LoanRequest(vehicle, loan, tenure, downPayment);
        } catch (InvalidInputException e) {
            throw new LoanLoadException("Data dari web service tidak valid: " + e.getMessage());
        }
    }

    private static String text(Map<String, Object> data, String field) {
        Object value = data.get(field);
        if (value == null) {
            throw new LoanLoadException("Field '" + field + "' tidak ada di response web service");
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof BigDecimal number) {
            return integerText(field, number);
        }
        throw new LoanLoadException("Field '" + field + "' harus berupa teks atau angka");
    }

    private static String integerText(String field, BigDecimal number) {
        if (number.signum() == 0) {
            return "0";
        }
        if (number.precision() - number.scale() > MAX_INTEGER_DIGITS) {
            throw new LoanLoadException("Field '" + field + "' terlalu besar");
        }
        BigDecimal stripped = number.stripTrailingZeros();
        if (stripped.scale() > 0) {
            throw new LoanLoadException(
                    "Field '" + field + "' harus bilangan bulat, bukan: " + number.toPlainString());
        }
        return stripped.toPlainString();
    }
}
