package bfst21.utils;

public enum VehicleType {
    CAR(130), //max speed Denmark
    BIKE(16), //from Google Maps 16 km/h
    WALK(5);  //from Google Maps 5 km/h

    private final double speed;  //km/h

    VehicleType(double speed) {
        this.speed = speed;
    }

    public double speed() {
        return speed;
    }
}
