package com.mycompany.carrentalsystem;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Car Rental System application.
 */
public class CarRentalSystem {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGui().setVisible(true));
    }
}
