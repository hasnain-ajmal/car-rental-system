package com.mycompany.carrentalsystem;

import java.io.Serializable;

/**
 * Abstract base class representing a car in the rental system.
 * Demonstrates OOP concepts: abstraction, encapsulation, and inheritance.
 */
public abstract class Car implements Serializable {

    private static final long serialVersionUID = 1L;

    private String brand;
    private String model;
    private String plateNumber;
    private double pricePerDay;
    private boolean available;

    public Car(String brand, String model, String plateNumber, double pricePerDay) {
        this.brand = brand;
        this.model = model;
        this.plateNumber = plateNumber.toUpperCase().trim();
        this.pricePerDay = pricePerDay;
        this.available = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getBrand()       { return brand; }
    public String getModel()       { return model; }
    public String getPlateNumber() { return plateNumber; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable()   { return available; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setAvailable(boolean available) { this.available = available; }

    // ── Behaviour ────────────────────────────────────────────────────────────

    /**
     * Calculates the rental cost for the given number of days.
     * Subclasses may override to apply surcharges or discounts.
     */
    public double calculateRent(int days) {
        return pricePerDay * days;
    }

    /** Returns the category label for this car (Economy / Luxury / SUV). */
    public abstract String getCarType();

    @Override
    public String toString() {
        return String.format("%s %s [%s] | Rs %.0f/day | %s",
                brand, model, plateNumber, pricePerDay,
                available ? "Available" : "Rented");
    }

    /** Unique display key used to match selection in combo boxes. */
    public String getDisplayKey() {
        return brand + " " + model + " [" + plateNumber + "]";
    }
}
