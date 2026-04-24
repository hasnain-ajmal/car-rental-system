package com.mycompany.carrentalsystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Main GUI for the Car Rental System.
 * Built with Java Swing — tabbed pane layout with five functional panels.
 */
public class MainGui extends JFrame {

    // ── Colour palette ───────────────────────────────────────────────────────
    private static final Color BG_MAIN    = new Color(240, 244, 248);
    private static final Color BG_PANEL   = new Color(255, 255, 255);
    private static final Color COLOR_BLUE = new Color(41, 128, 185);
    private static final Color COLOR_GREEN= new Color(39, 174,  96);
    private static final Color COLOR_RED  = new Color(192,  57,  43);
    private static final Color COLOR_DARK = new Color(44,  62,  80);
    private static final Font  FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD,  14);

    // ── Core service ─────────────────────────────────────────────────────────
    private final RentalSystem system = new RentalSystem();

    // ── Add-Car fields ───────────────────────────────────────────────────────
    private JTextField  tfBrand, tfModel, tfPlate, tfPrice;
    private JComboBox<String> cbCarType;

    // ── Rent-Car fields ──────────────────────────────────────────────────────
    private JComboBox<String> cbRentType, cbRentCar;
    private JTextField  tfCustName, tfCustPhone, tfCustCnic;
    private JSpinner    spDays;

    // ── Return-Car fields ────────────────────────────────────────────────────
    private JComboBox<String> cbReturnCar;

    // ── View-Cars table ──────────────────────────────────────────────────────
    private DefaultTableModel tmAvailable, tmRented;
    private JTextField tfSearch;

    // ── Rental History table ─────────────────────────────────────────────────
    private DefaultTableModel tmHistory;

    // ─────────────────────────────────────────────────────────────────────────

    public MainGui() {
        applySystemLookAndFeel();
        setTitle("Car Rental System");
        setSize(750, 520);
        setMinimumSize(new Dimension(650, 460));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT_TITLE);
        tabs.addTab("  Add Car  ",     buildAddCarPanel());
        tabs.addTab("  Rent Car  ",    buildRentCarPanel());
        tabs.addTab("  Return Car  ",  buildReturnCarPanel());
        tabs.addTab("  View Cars  ",   buildViewCarsPanel());
        tabs.addTab("  History  ",     buildHistoryPanel());

        add(tabs);
        refreshAll();
    }

    // ═════════════════════════  TAB 1 — ADD CAR  ═════════════════════════════

    private JPanel buildAddCarPanel() {
        JPanel panel = formPanel(new Color(245, 250, 255));

        tfBrand = new JTextField(15);
        tfModel = new JTextField(15);
        tfPlate = new JTextField(15);
        tfPrice = new JTextField(15);
        cbCarType = new JComboBox<>(new String[]{"Economy", "Luxury", "SUV"});

        int row = 0;
        addRow(panel, row++, "Brand:",      tfBrand);
        addRow(panel, row++, "Model:",      tfModel);
        addRow(panel, row++, "Plate No:",   tfPlate);
        addRow(panel, row++, "Price/Day (Rs):", tfPrice);
        addRow(panel, row++, "Car Type:",   cbCarType);

        JButton btnAdd = styledButton("Add Car", COLOR_BLUE);
        btnAdd.addActionListener(e -> onAddCar());
        addWideButton(panel, row, btnAdd);

        return panel;
    }

    private void onAddCar() {
        String brand = tfBrand.getText().trim();
        String model = tfModel.getText().trim();
        String plate = tfPlate.getText().trim();
        String priceText = tfPrice.getText().trim();

        if (brand.isEmpty() || model.isEmpty() || plate.isEmpty() || priceText.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (system.plateExists(plate)) {
            showError("A car with plate number \"" + plate + "\" already exists.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Enter a valid positive number for Price/Day.");
            return;
        }

        String type = (String) cbCarType.getSelectedItem();
        Car car = switch (type) {
            case "Luxury" -> new LuxuryCar(brand, model, plate, price);
            case "SUV"    -> new SUVCar(brand, model, plate, price);
            default       -> new EconomyCar(brand, model, plate, price);
        };

        system.addCar(car);
        showInfo("Car added successfully!\n" + car.getDisplayKey());
        tfBrand.setText(""); tfModel.setText(""); tfPlate.setText(""); tfPrice.setText("");
        refreshAll();
    }

    // ════════════════════════  TAB 2 — RENT CAR  ═════════════════════════════

    private JPanel buildRentCarPanel() {
        JPanel panel = formPanel(new Color(245, 255, 250));

        cbRentType = new JComboBox<>(new String[]{"Economy", "Luxury", "SUV"});
        cbRentType.addActionListener(e -> refreshRentCarBox());
        cbRentCar  = new JComboBox<>();

        tfCustName  = new JTextField(15);
        tfCustPhone = new JTextField(15);
        tfCustCnic  = new JTextField(15);

        spDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spDays.setFont(FONT_LABEL);

        int row = 0;
        addRow(panel, row++, "Car Type:",      cbRentType);
        addRow(panel, row++, "Select Car:",    cbRentCar);
        addRow(panel, row++, "Rental Days:",   spDays);
        addRow(panel, row++, "Customer Name:", tfCustName);
        addRow(panel, row++, "Phone:",         tfCustPhone);
        addRow(panel, row++, "CNIC:",          tfCustCnic);

        JButton btnRent = styledButton("Rent Car & Generate Bill", COLOR_GREEN);
        btnRent.addActionListener(e -> onRentCar());
        addWideButton(panel, row, btnRent);

        return panel;
    }

    private void refreshRentCarBox() {
        cbRentCar.removeAllItems();
        String type = (String) cbRentType.getSelectedItem();
        for (Car c : system.getCars()) {
            if (c.isAvailable() && c.getCarType().equals(type)) {
                cbRentCar.addItem(c.getDisplayKey());
            }
        }
    }

    private void onRentCar() {
        if (cbRentCar.getItemCount() == 0) {
            showError("No available cars of the selected type.");
            return;
        }
        String name  = tfCustName.getText().trim();
        String phone = tfCustPhone.getText().trim();
        String cnic  = tfCustCnic.getText().trim();
        int days = (int) spDays.getValue();

        if (name.isEmpty() || phone.isEmpty() || cnic.isEmpty()) {
            showError("Please fill in all customer details.");
            return;
        }

        String selectedKey = (String) cbRentCar.getSelectedItem();
        Car selectedCar = findCarByKey(selectedKey);
        if (selectedCar == null) { showError("Selected car not found."); return; }

        Customer customer = new Customer(name, phone, cnic);
        RentalRecord record = system.rentCar(selectedCar, customer, days);

        if (record == null) {
            showError("This car is no longer available.");
        } else {
            showBill("Rental Successful — Bill", record.getBillSummary());
            tfCustName.setText(""); tfCustPhone.setText(""); tfCustCnic.setText("");
            spDays.setValue(1);
        }
        refreshAll();
    }

    // ═══════════════════════  TAB 3 — RETURN CAR  ════════════════════════════

    private JPanel buildReturnCarPanel() {
        JPanel panel = formPanel(new Color(255, 250, 245));

        cbReturnCar = new JComboBox<>();

        int row = 0;
        addRow(panel, row++, "Select Rented Car:", cbReturnCar);

        JButton btnReturn = styledButton("Return Car", COLOR_RED);
        btnReturn.addActionListener(e -> onReturnCar());
        addWideButton(panel, row, btnReturn);

        return panel;
    }

    private void onReturnCar() {
        if (cbReturnCar.getItemCount() == 0) {
            showError("No cars are currently rented.");
            return;
        }
        String key = (String) cbReturnCar.getSelectedItem();
        Car car = findCarByKey(key);
        if (car == null) { showError("Car not found."); return; }

        RentalRecord record = system.returnCar(car);
        if (record != null) {
            String summary = record.getBillSummary()
                + "\n─────────────────────────────────"
                + "\nReturn Date : " + record.getReturnDateStr();
            showBill("Car Returned — Final Bill", summary);
        } else {
            showInfo("Car returned successfully.");
        }
        refreshAll();
    }

    // ═══════════════════════  TAB 4 — VIEW CARS  ══════════════════════════════

    private JPanel buildViewCarsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchBar.setBackground(BG_PANEL);
        searchBar.add(new JLabel("Search:"));
        tfSearch = new JTextField(20);
        tfSearch.setFont(FONT_LABEL);
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTables(); }
        });
        searchBar.add(tfSearch);

        JButton btnRemove = styledButton("Remove Selected", COLOR_RED);
        btnRemove.addActionListener(e -> onRemoveCar());
        searchBar.add(btnRemove);

        // Tables
        String[] cols = {"Brand", "Model", "Plate", "Type", "Price/Day", "Status"};
        tmAvailable = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tmRented = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tblAvailable = styledTable(tmAvailable);
        JTable tblRented    = styledTable(tmRented);

        JScrollPane spAvail  = new JScrollPane(tblAvailable);
        JScrollPane spRented = new JScrollPane(tblRented);

        spAvail.setBorder(titledBorder("Available Cars"));
        spRented.setBorder(titledBorder("Rented Cars"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spAvail, spRented);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void onRemoveCar() {
        // Show list of available (removable) cars in a dialog
        List<Car> available = system.getAvailableCars();
        if (available.isEmpty()) { showError("No available cars to remove."); return; }

        String[] options = available.stream()
                .map(Car::getDisplayKey).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(
                this, "Select car to remove:", "Remove Car",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (chosen == null) return;

        Car car = findCarByKey(chosen);
        if (car != null && system.removeCar(car)) {
            showInfo("Car removed: " + chosen);
            refreshAll();
        } else {
            showError("Could not remove car (it may currently be rented).");
        }
    }

    private void filterTables() {
        String q = tfSearch.getText().trim().toLowerCase();
        updateViewTables(q);
    }

    // ═══════════════════════  TAB 5 — HISTORY  ════════════════════════════════

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Customer", "Phone", "Car", "Type", "Days", "Total (Rs)", "Rented On", "Returned On", "Status"};
        tmHistory = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = styledTable(tmHistory);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tmHistory);
        tbl.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(titledBorder("All Rental Transactions"));

        JButton btnRefresh = styledButton("Refresh", COLOR_BLUE);
        btnRefresh.addActionListener(e -> updateHistoryTable());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG_PANEL);
        bottom.add(btnRefresh);

        panel.add(sp, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ═══════════════════════  REFRESH HELPERS  ════════════════════════════════

    private void refreshAll() {
        updateViewTables("");
        refreshRentCarBox();
        refreshReturnBox();
        updateHistoryTable();
        if (tfSearch != null) tfSearch.setText("");
    }

    private void updateViewTables(String query) {
        tmAvailable.setRowCount(0);
        tmRented.setRowCount(0);
        for (Car c : system.getCars()) {
            String row5 = String.format("%.0f", c.getPricePerDay());
            Object[] row = {c.getBrand(), c.getModel(), c.getPlateNumber(),
                            c.getCarType(), row5,
                            c.isAvailable() ? "Available" : "Rented"};
            boolean matches = query.isEmpty() ||
                    c.getBrand().toLowerCase().contains(query) ||
                    c.getModel().toLowerCase().contains(query) ||
                    c.getPlateNumber().toLowerCase().contains(query) ||
                    c.getCarType().toLowerCase().contains(query);
            if (!matches) continue;
            if (c.isAvailable()) tmAvailable.addRow(row);
            else                 tmRented.addRow(row);
        }
    }

    private void refreshReturnBox() {
        cbReturnCar.removeAllItems();
        for (Car c : system.getRentedCars()) {
            cbReturnCar.addItem(c.getDisplayKey());
        }
    }

    private void updateHistoryTable() {
        tmHistory.setRowCount(0);
        for (RentalRecord r : system.getAllRecords()) {
            tmHistory.addRow(new Object[]{
                r.getCustomer().getName(),
                r.getCustomer().getPhone(),
                r.getCar().getBrand() + " " + r.getCar().getModel(),
                r.getCar().getCarType(),
                r.getDays(),
                String.format("%.2f", r.getTotalCost()),
                r.getRentalDateStr(),
                r.getReturnDateStr(),
                r.getStatus()
            });
        }
    }

    // ═══════════════════════  UI UTILITIES  ════════════════════════════════════

    private JPanel formPanel(Color bg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bg);
        p.setBorder(new EmptyBorder(20, 40, 20, 40));
        return p;
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_DARK);

        if (field instanceof JTextField tf) tf.setFont(FONT_LABEL);

        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void addWideButton(JPanel panel, int row, JButton btn) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 8, 7, 8);
        panel.add(btn, gbc);
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_LABEL);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(COLOR_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        return table;
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_DARK, 1), title);
        tb.setTitleFont(FONT_TITLE);
        tb.setTitleColor(COLOR_DARK);
        return tb;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showBill(String title, String billText) {
        JTextArea area = new JTextArea(billText);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(250, 250, 250));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ═══════════════════════  LOOKUP HELPER  ══════════════════════════════════

    private Car findCarByKey(String key) {
        if (key == null) return null;
        for (Car c : system.getCars()) {
            if (c.getDisplayKey().equals(key)) return c;
        }
        return null;
    }

    // ═══════════════════════  LOOK & FEEL  ════════════════════════════════════

    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }
}
