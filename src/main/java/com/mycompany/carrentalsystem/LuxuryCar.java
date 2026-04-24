package com.mycompany.carrentalsystem;

/**
 * Luxury car — flat Rs 2,000 surcharge added on top of the base rental cost.
 */
public class LuxuryCar extends Car {

    private static final long serialVersionUID = 1L;
    private static final double LUXURY_SURCHARGE = 2000.0;

    public LuxuryCar(String brand, String model, String plateNumber, double pricePerDay) {
        super(brand, model, plateNumber, pricePerDay);
    }

    @Override
    public double calculateRent(int days) {
        return super.calculateRent(days) + LUXURY_SURCHARGE;
    }

    @Override
    public String getCarType() {
        return "Luxury";
    }
}
