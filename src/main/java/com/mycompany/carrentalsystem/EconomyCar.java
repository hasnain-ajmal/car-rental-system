package com.mycompany.carrentalsystem;

/**
 * Economy car — base price with no surcharge.
 */
public class EconomyCar extends Car {

    private static final long serialVersionUID = 1L;

    public EconomyCar(String brand, String model, String plateNumber, double pricePerDay) {
        super(brand, model, plateNumber, pricePerDay);
    }

    @Override
    public String getCarType() {
        return "Economy";
    }
}
