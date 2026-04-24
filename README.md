# Car Rental System

A desktop application built in **Java** using **Java Swing** for the GUI and **Maven** for build management.
Developed as an OOP course final project (4th Semester).

---

## Features

| Tab | What it does |
|-----|-------------|
| **Add Car** | Add Economy, Luxury, or SUV cars with unique plate numbers |
| **Rent Car** | Rent a car to a customer — choose rental days and generate a bill |
| **Return Car** | Return a rented car and view the final bill |
| **View Cars** | Live search/filter across all cars; separate tables for available and rented |
| **History** | Full rental transaction history with dates, cost, and status |

---

## OOP Concepts Demonstrated

- **Inheritance** — `EconomyCar`, `LuxuryCar`, `SUVCar` all extend abstract `Car`
- **Polymorphism** — `calculateRent(int days)` is overridden in each subclass with different pricing logic
- **Abstraction** — `Car` is an abstract class; `Rentable` is an interface
- **Encapsulation** — all fields are private with controlled access through getters/setters
- **Interface** — `Rentable` defines the contract (`rentCar`, `returnCar`) implemented by `RentalSystem`

---

## Project Structure

```
CarRentalSystem/
├── src/main/java/com/mycompany/carrentalsystem/
│   ├── Car.java             # Abstract base class
│   ├── EconomyCar.java      # Economy — base price
│   ├── LuxuryCar.java       # Luxury — Rs 2,000 surcharge
│   ├── SUVCar.java          # SUV    — 15% surcharge
│   ├── Customer.java        # Customer model
│   ├── RentalRecord.java    # Rental transaction record
│   ├── Rentable.java        # Interface
│   ├── RentalSystem.java    # Business logic + file persistence
│   ├── MainGui.java         # Java Swing GUI
│   └── CarRentalSystem.java # Entry point
├── data/                    # Runtime data (serialized, git-ignored)
├── pom.xml
└── README.md
```

---

## How to Run

### Prerequisites
- Java 21+
- Maven 3.6+

### Build & Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/car-rental-system.git
cd car-rental-system

# Build a runnable JAR
mvn package

# Run
java -jar target/CarRentalSystem.jar
```

Or open in **NetBeans / IntelliJ IDEA** and run `CarRentalSystem.java` directly.

---

## Pricing Logic

| Car Type | Formula |
|----------|---------|
| Economy  | `price × days` |
| Luxury   | `price × days + Rs 2,000` |
| SUV      | `price × days × 1.15` |

---

## Tech Stack

- **Language:** Java 21
- **GUI:** Java Swing
- **Build Tool:** Maven
- **Persistence:** Java Object Serialization
