package com.mycompany.carrentalsystem;

/**
 * Contract that any rental service must fulfil.
 * Demonstrates the Interface segregation / abstraction OOP principle.
 */
public interface Rentable {

    /**
     * Rents a car to a customer for a specified number of days.
     *
     * @param car      the car to rent
     * @param customer the customer renting the car
     * @param days     rental duration in days
     * @return a RentalRecord for the transaction, or null if unavailable
     */
    RentalRecord rentCar(Car car, Customer customer, int days);

    /**
     * Processes the return of a rented car.
     *
     * @param car the car being returned
     * @return the updated RentalRecord, or null if no active rental found
     */
    RentalRecord returnCar(Car car);
}
