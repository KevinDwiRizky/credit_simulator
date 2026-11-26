package factory;

import exception.InvalidInputException;
import model.Car;
import model.Motor;
import model.Vehicle;
import model.VehicleCondition;

public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static String normalizeType(String type) {
        if (type == null) {
            throw new InvalidInputException("Jenis kendaraan tidak boleh kosong");
        }
        String normalized = type.trim().toLowerCase();
        if (!normalized.equals("mobil") && !normalized.equals("motor")) {
            throw new InvalidInputException("Jenis kendaraan harus Motor atau Mobil, bukan: " + type);
        }
        return normalized;
    }

    public static Vehicle create(String type, String condition, int year) {
        String normalizedType = normalizeType(type);
        VehicleCondition vehicleCondition = VehicleCondition.fromText(condition);

        if (normalizedType.equals("mobil")) {
            return new Car(vehicleCondition, year);
        }
        return new Motor(vehicleCondition, year);
    }
}