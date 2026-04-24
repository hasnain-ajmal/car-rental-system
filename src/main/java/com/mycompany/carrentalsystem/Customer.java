package com.mycompany.carrentalsystem;

import java.io.Serializable;

/**
 * Represents a customer who can rent a car.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String phone;
    private String cnic;        // Pakistani CNIC (e.g. 12345-1234567-1)

    public Customer(String name, String phone, String cnic) {
        this.name  = name;
        this.phone = phone;
        this.cnic  = cnic;
    }

    public String getName()  { return name; }
    public String getPhone() { return phone; }
    public String getCnic()  { return cnic; }

    @Override
    public String toString() {
        return name + " | " + phone + " | CNIC: " + cnic;
    }
}
