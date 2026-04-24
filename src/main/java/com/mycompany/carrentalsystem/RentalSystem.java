package com.mycompany.carrentalsystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core service layer for the Car Rental System.
 * Implements {@link Rentable} and manages persistence via Java serialization.
 */
public class RentalSystem implements Rentable {

    private static final String CARS_FILE    = "data/cars.dat";
    private static final String RECORDS_FILE = "data/records.dat";

    private ArrayList<Car>          cars    = new ArrayList<>();
    private ArrayList<RentalRecord> records = new ArrayList<>();

    public RentalSystem() {
        new File("data").mkdirs();   // ensure data directory exists
        loadCars();
        loadRecords();
    }

    // ── Car management ───────────────────────────────────────────────────────

    public void addCar(Car car) {
        cars.add(car);
        saveCars();
    }

    public boolean removeCar(Car car) {
        if (!car.isAvailable()) return false;   // cannot remove a rented car
        cars.remove(car);
        saveCars();
        return true;
    }

    public ArrayList<Car> getCars() { return cars; }

    public List<Car> getAvailableCars() {
        return cars.stream().filter(Car::isAvailable).collect(Collectors.toList());
    }

    public List<Car> getRentedCars() {
        return cars.stream().filter(c -> !c.isAvailable()).collect(Collectors.toList());
    }

    public boolean plateExists(String plateNumber) {
        return cars.stream()
                   .anyMatch(c -> c.getPlateNumber().equalsIgnoreCase(plateNumber.trim()));
    }

    // ── Rentable implementation ──────────────────────────────────────────────

    @Override
    public RentalRecord rentCar(Car car, Customer customer, int days) {
        if (!car.isAvailable()) return null;
        car.setAvailable(false);
        RentalRecord record = new RentalRecord(customer, car, days);
        records.add(record);
        saveCars();
        saveRecords();
        return record;
    }

    @Override
    public RentalRecord returnCar(Car car) {
        // Find the active rental record for this car
        for (RentalRecord r : records) {
            if (r.getCar() == car && !r.isReturned()) {
                r.markReturned();
                car.setAvailable(true);
                saveCars();
                saveRecords();
                return r;
            }
        }
        return null;
    }

    // ── Rental history ───────────────────────────────────────────────────────

    public ArrayList<RentalRecord> getAllRecords()    { return records; }

    public List<RentalRecord> getActiveRecords() {
        return records.stream().filter(r -> !r.isReturned()).collect(Collectors.toList());
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    public void saveCars() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CARS_FILE))) {
            oos.writeObject(cars);
        } catch (IOException e) {
            System.err.println("Error saving cars: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadCars() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(CARS_FILE))) {
            cars = (ArrayList<Car>) ois.readObject();
        } catch (Exception e) {
            cars = new ArrayList<>();
        }
    }

    public void saveRecords() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(RECORDS_FILE))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.err.println("Error saving records: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadRecords() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(RECORDS_FILE))) {
            records = (ArrayList<RentalRecord>) ois.readObject();
        } catch (Exception e) {
            records = new ArrayList<>();
        }
    }
}
