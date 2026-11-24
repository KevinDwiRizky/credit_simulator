package factory;


import exception.InvalidInputException;
import model.Car;
import model.Motor;
import model.Vehicle;
import model.VehicleCondition;

public final class VehicleFactory {

    private VehicleFactory() {
        // class utilitas, tidak perlu di-new
    }

    public static Vehicle create(String type, String condition, int year) {
        if (type == null) {
            throw new InvalidInputException("Jenis kendaraan tidak boleh kosong");
        }
        VehicleCondition vehicleCondition = VehicleCondition.fromText(condition);

        switch (type.trim().toLowerCase()) {
            case "mobil":
                return new Car(vehicleCondition, year);
            case "motor":
                return new Motor(vehicleCondition, year);
            default:
                throw new InvalidInputException("Jenis kendaraan harus Motor atau Mobil, bukan: " + type);
        }
    }
}
