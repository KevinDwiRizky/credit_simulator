package model;

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
}