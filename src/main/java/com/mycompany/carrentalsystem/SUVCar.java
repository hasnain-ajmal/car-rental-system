package com.mycompany.carrentalsystem;

/**
 * SUV car — 15 % surcharge applied on top of the base rental cost.
 * Demonstrates OOP extensibility: adding a new car type requires
 * only a new subclass with the overridden calculateRent() method.
 */
public class SUVCar extends Car {

    private static final long serialVersionUID = 1L;
    private static final double SURCHARGE_RATE = 0.15;

    public SUVCar(String brand, String model, String plateNumber, double pricePerDay) {
        super(brand, model, plateNumber, pricePerDay);
    }

    @Override
    public double calculateRent(int days) {
        double baseCost = super.calculateRent(days);
        return baseCost + (baseCost * SURCHARGE_RATE);
    }

    @Override
    public String getCarType() {
        return "SUV";
    }
}
