package model;

import exception.InvalidInputException;

public enum VehicleCondition {
    NEW("Baru"),
    USED("Bekas");

    private final String label;

    VehicleCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static VehicleCondition fromText(String text) {
        if (text == null) {
            throw new InvalidInputException("Kondisi kendaraan tidak boleh kosong");
        }
        String normalized = text.trim();
        for (VehicleCondition c : values()) {
            if (c.label.equalsIgnoreCase(normalized)) {
                return c;
            }
        }
        throw new InvalidInputException("Kondisi kendaraan harus Baru atau Bekas, bukan: " + text);
    }
}