package com.mycompany.carrentalsystem;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single car rental transaction.
 * Stores who rented what, for how long, at what cost, and whether it has been returned.
 */
public class RentalRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Customer customer;
    private final Car car;
    private final int days;
    private final double totalCost;
    private final LocalDate rentalDate;
    private boolean returned;
    private LocalDate returnDate;

    public RentalRecord(Customer customer, Car car, int days) {
        this.customer   = customer;
        this.car        = car;
        this.days       = days;
        this.totalCost  = car.calculateRent(days);
        this.rentalDate = LocalDate.now();
        this.returned   = false;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Customer  getCustomer()   { return customer; }
    public Car       getCar()        { return car; }
    public int       getDays()       { return days; }
    public double    getTotalCost()  { return totalCost; }
    public LocalDate getRentalDate() { return rentalDate; }
    public boolean   isReturned()    { return returned; }
    public LocalDate getReturnDate() { return returnDate; }

    // ── Actions ──────────────────────────────────────────────────────────────

    public void markReturned() {
        this.returned   = true;
        this.returnDate = LocalDate.now();
    }

    // ── Display helpers ──────────────────────────────────────────────────────

    public String getRentalDateStr() { return rentalDate.format(FORMATTER); }

    public String getReturnDateStr() {
        return returned ? returnDate.format(FORMATTER) : "—";
    }

    public String getStatus() { return returned ? "Returned" : "Active"; }

    /** Summary used in bill dialogs. */
    public String getBillSummary() {
        return String.format(
            "Customer : %s%n" +
            "Phone    : %s%n" +
            "CNIC     : %s%n" +
            "─────────────────────────────────%n" +
            "Car      : %s %s%n" +
            "Type     : %s%n" +
            "Plate    : %s%n" +
            "Days     : %d%n" +
            "Rate     : Rs %.0f / day%n" +
            "─────────────────────────────────%n" +
            "Total    : Rs %.2f%n" +
            "Date     : %s",
            customer.getName(), customer.getPhone(), customer.getCnic(),
            car.getBrand(), car.getModel(),
            car.getCarType(), car.getPlateNumber(),
            days, car.getPricePerDay(),
            totalCost, getRentalDateStr()
        );
    }
}
