package com.mycompany.carrentalsystem;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class MainGui extends JFrame {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color ACCENT   = new Color(99, 102, 241);
    private static final Color SUCCESS  = new Color(34, 197,  94);
    private static final Color DANGER   = new Color(239,  68,  68);
    private static final Color TEXT_DIM = new Color(148, 163, 184);

    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  20);

    // ── Service ───────────────────────────────────────────────────────────────
    private final RentalSystem system = new RentalSystem();

    // ── Add Car ───────────────────────────────────────────────────────────────
    private JTextField tfBrand, tfModel, tfPlate, tfPrice;
    private JComboBox<String> cbCarType;

    // ── Rent Car ──────────────────────────────────────────────────────────────
    private JComboBox<String> cbRentType, cbRentCar;
    private JTextField tfCustName, tfCustPhone, tfCustCnic;
    private JSpinner spDays;

    // ── Return Car ────────────────────────────────────────────────────────────
    private JComboBox<String> cbReturnCar;

    // ── View Cars ─────────────────────────────────────────────────────────────
    private DefaultTableModel tmAvailable, tmRented;
    private JTextField tfSearch;

    // ── History ───────────────────────────────────────────────────────────────
    private DefaultTableModel tmHistory;

    // ── Header stats ─────────────────────────────────────────────────────────
    private JLabel lblStats;

    // ─────────────────────────────────────────────────────────────────────────

    public MainGui() {
        setTitle("Car Rental System");
        setSize(860, 580);
        setMinimumSize(new Dimension(720, 480));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);

        refreshAll();
    }

    // ═══════════════════════  HEADER  ════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(ACCENT);
        h.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("Car Rental System");
        title.setFont(FONT_HEADER);
        title.setForeground(Color.WHITE);

        lblStats = new JLabel();
        lblStats.setFont(FONT_BOLD);
        lblStats.setForeground(new Color(199, 210, 254));

        h.add(title,    BorderLayout.WEST);
        h.add(lblStats, BorderLayout.EAST);
        return h;
    }

    private void refreshStats() {
        int total     = system.getCars().size();
        int available = system.getAvailableCars().size();
        int rented    = total - available;
        lblStats.setText("Total: " + total + "   Available: " + available + "   Rented: " + rented);
    }

    // ═══════════════════════  TABS  ══════════════════════════════════════════

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT_BOLD);
        tabs.addTab("  Add Car  ",    buildAddCarPanel());
        tabs.addTab("  Rent Car  ",   buildRentCarPanel());
        tabs.addTab("  Return Car  ", buildReturnCarPanel());
        tabs.addTab("  View Cars  ",  buildViewCarsPanel());
        tabs.addTab("  History  ",    buildHistoryPanel());
        return tabs;
    }

    // ═══════════════════════  TAB 1 — ADD CAR  ═══════════════════════════════

    private JPanel buildAddCarPanel() {
        tfBrand   = field("e.g. Toyota");
        tfModel   = field("e.g. Corolla");
        tfPlate   = field("e.g. ABC-123");
        tfPrice   = field("e.g. 5000");
        cbCarType = combo("Economy", "Luxury", "SUV");

        Object[][] rows = {
            {"Brand",            tfBrand},
            {"Model",            tfModel},
            {"Plate Number",     tfPlate},
            {"Price / Day (Rs)", tfPrice},
            {"Car Type",         cbCarType},
        };

        JButton btn = btn("Add Car", ACCENT);
        btn.addActionListener(e -> onAddCar());

        return formPanel("Add New Car", rows, btn);
    }

    private void onAddCar() {
        String brand = tfBrand.getText().trim();
        String model = tfModel.getText().trim();
        String plate = tfPlate.getText().trim();
        String priceText = tfPrice.getText().trim();

        if (brand.isEmpty() || model.isEmpty() || plate.isEmpty() || priceText.isEmpty()) {
            err("Please fill in all fields."); return;
        }
        if (system.plateExists(plate)) {
            err("Plate \"" + plate + "\" already exists."); return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            err("Enter a valid positive number for Price/Day."); return;
        }

        String type = (String) cbCarType.getSelectedItem();
        Car car = switch (type) {
            case "Luxury" -> new LuxuryCar(brand, model, plate, price);
            case "SUV"    -> new SUVCar(brand, model, plate, price);
            default       -> new EconomyCar(brand, model, plate, price);
        };
        system.addCar(car);
        ok("Car added: " + car.getDisplayKey());
        tfBrand.setText(""); tfModel.setText(""); tfPlate.setText(""); tfPrice.setText("");
        refreshAll();
    }

    // ═══════════════════════  TAB 2 — RENT CAR  ══════════════════════════════

    private JPanel buildRentCarPanel() {
        cbRentType = combo("Economy", "Luxury", "SUV");
        cbRentType.addActionListener(e -> refreshRentCarBox());
        cbRentCar   = combo();
        tfCustName  = field("Full name");
        tfCustPhone = field("03XX-XXXXXXX");
        tfCustCnic  = field("XXXXX-XXXXXXX-X");
        spDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spDays.setFont(FONT_BODY);

        Object[][] rows = {
            {"Car Type",       cbRentType},
            {"Select Car",     cbRentCar},
            {"Rental Days",    spDays},
            {"Customer Name",  tfCustName},
            {"Phone",          tfCustPhone},
            {"CNIC",           tfCustCnic},
        };

        JButton btn = btn("Rent & Generate Bill", SUCCESS);
        btn.addActionListener(e -> onRentCar());

        return formPanel("Rent a Car", rows, btn);
    }

    private void refreshRentCarBox() {
        if (cbRentCar == null) return;
        cbRentCar.removeAllItems();
        String type = (String) cbRentType.getSelectedItem();
        for (Car c : system.getCars())
            if (c.isAvailable() && c.getCarType().equals(type))
                cbRentCar.addItem(c.getDisplayKey());
    }

    private void onRentCar() {
        if (cbRentCar.getItemCount() == 0) { err("No available cars of this type."); return; }
        String name  = tfCustName.getText().trim();
        String phone = tfCustPhone.getText().trim();
        String cnic  = tfCustCnic.getText().trim();
        int days = (int) spDays.getValue();
        if (name.isEmpty() || phone.isEmpty() || cnic.isEmpty()) { err("Fill in all customer details."); return; }

        Car car = findByKey((String) cbRentCar.getSelectedItem());
        if (car == null) { err("Car not found."); return; }

        RentalRecord rec = system.rentCar(car, new Customer(name, phone, cnic), days);
        if (rec == null) { err("Car is no longer available."); return; }

        bill("Rental Bill", rec.getBillSummary());
        tfCustName.setText(""); tfCustPhone.setText(""); tfCustCnic.setText("");
        spDays.setValue(1);
        refreshAll();
    }

    // ═══════════════════════  TAB 3 — RETURN CAR  ════════════════════════════

    private JPanel buildReturnCarPanel() {
        cbReturnCar = combo();

        Object[][] rows = {{"Select Rented Car", cbReturnCar}};

        JButton btn = btn("Return Car", DANGER);
        btn.addActionListener(e -> onReturnCar());

        return formPanel("Return a Car", rows, btn);
    }

    private void onReturnCar() {
        if (cbReturnCar.getItemCount() == 0) { err("No cars are currently rented."); return; }
        Car car = findByKey((String) cbReturnCar.getSelectedItem());
        if (car == null) { err("Car not found."); return; }

        RentalRecord rec = system.returnCar(car);
        String msg = rec != null
            ? rec.getBillSummary() + "\n─────────────────────────────────\nReturn Date : " + rec.getReturnDateStr()
            : "Car returned successfully.";
        bill("Final Bill", msg);
        refreshAll();
    }

    // ═══════════════════════  TAB 4 — VIEW CARS  ══════════════════════════════

    private JPanel buildViewCarsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Search + Remove bar
        tfSearch = field("Search by brand, model, plate...");
        tfSearch.setPreferredSize(new Dimension(240, 30));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTables(); }
        });

        JButton btnRemove = btn("Remove Car", DANGER);
        btnRemove.setPreferredSize(new Dimension(130, 30));
        btnRemove.addActionListener(e -> onRemoveCar());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setOpaque(false);
        JLabel lbl = new JLabel("Search:");
        lbl.setFont(FONT_BOLD);
        bar.add(lbl); bar.add(tfSearch); bar.add(btnRemove);

        // Tables
        String[] cols = {"Brand", "Model", "Plate", "Type", "Price/Day (Rs)", "Status"};
        tmAvailable = tableModel(cols);
        tmRented    = tableModel(cols);

        JTable tblAvail  = makeTable(tmAvailable);
        JTable tblRented = makeTable(tmRented);

        // Colour status column
        colorStatusCol(tblAvail,  5, "Available", SUCCESS);
        colorStatusCol(tblRented, 5, "Rented",    DANGER);

        JScrollPane spAvail  = new JScrollPane(tblAvail);
        JScrollPane spRented = new JScrollPane(tblRented);
        spAvail.setBorder(BorderFactory.createTitledBorder("  Available Cars"));
        spRented.setBorder(BorderFactory.createTitledBorder("  Rented Cars"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spAvail, spRented);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        panel.add(bar,   BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void onRemoveCar() {
        List<Car> avail = system.getAvailableCars();
        if (avail.isEmpty()) { err("No available cars to remove."); return; }
        String[] opts = avail.stream().map(Car::getDisplayKey).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select car to remove:", "Remove Car",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (chosen == null) return;
        Car car = findByKey(chosen);
        if (car != null && system.removeCar(car)) {
            ok("Removed: " + chosen); refreshAll();
        } else {
            err("Cannot remove a currently rented car.");
        }
    }

    private void filterTables() {
        String q = tfSearch.getText().trim().toLowerCase();
        tmAvailable.setRowCount(0);
        tmRented.setRowCount(0);
        for (Car c : system.getCars()) {
            boolean match = q.isEmpty()
                || c.getBrand().toLowerCase().contains(q)
                || c.getModel().toLowerCase().contains(q)
                || c.getPlateNumber().toLowerCase().contains(q)
                || c.getCarType().toLowerCase().contains(q);
            if (!match) continue;
            Object[] row = {c.getBrand(), c.getModel(), c.getPlateNumber(),
                            c.getCarType(), String.format("%.0f", c.getPricePerDay()),
                            c.isAvailable() ? "Available" : "Rented"};
            if (c.isAvailable()) tmAvailable.addRow(row);
            else                 tmRented.addRow(row);
        }
    }

    // ═══════════════════════  TAB 5 — HISTORY  ════════════════════════════════

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Customer", "Phone", "Car", "Type", "Days", "Total (Rs)",
                         "Rented On", "Returned On", "Status"};
        tmHistory = tableModel(cols);
        JTable tbl = makeTable(tmHistory);
        tbl.setRowSorter(new TableRowSorter<>(tmHistory));

        // Colour status column
        tbl.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                if (!sel) setForeground("Active".equals(v) ? SUCCESS : TEXT_DIM);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createTitledBorder(
                "  All Rental Transactions  (click a column header to sort)"));

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════  REFRESH  ════════════════════════════════════════

    private void refreshAll() {
        filterTables();
        if (tfSearch != null) tfSearch.setText("");
        refreshRentCarBox();
        if (cbReturnCar != null) {
            cbReturnCar.removeAllItems();
            for (Car c : system.getRentedCars()) cbReturnCar.addItem(c.getDisplayKey());
        }
        // History
        if (tmHistory != null) {
            tmHistory.setRowCount(0);
            for (RentalRecord r : system.getAllRecords()) {
                tmHistory.addRow(new Object[]{
                    r.getCustomer().getName(), r.getCustomer().getPhone(),
                    r.getCar().getBrand() + " " + r.getCar().getModel(),
                    r.getCar().getCarType(), r.getDays(),
                    String.format("%.2f", r.getTotalCost()),
                    r.getRentalDateStr(), r.getReturnDateStr(), r.getStatus()
                });
            }
        }
        refreshStats();
    }

    // ═══════════════════════  FORM BUILDER  ══════════════════════════════════

    /**
     * Builds a centered form panel with label-field rows and a bottom button.
     * rows: Object[][] where each row is {String label, JComponent field}
     */
    private JPanel formPanel(String title, Object[][] rows, JButton actionBtn) {
        // Outer wrapper to centre the card
        JPanel outer = new JPanel(new GridBagLayout());

        // Card panel
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 36, 28, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_TITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 14, 8);
        card.add(lbl, gbc);

        // Separator
        JSeparator sep = new JSeparator();
        gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        card.add(sep, gbc);

        // Rows
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        for (int i = 0; i < rows.length; i++) {
            JLabel rowLabel = new JLabel((String) rows[i][0]);
            rowLabel.setFont(FONT_BOLD);
            rowLabel.setPreferredSize(new Dimension(160, 26));

            JComponent field = (JComponent) rows[i][1];

            gbc.gridx = 0; gbc.gridy = i + 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            card.add(rowLabel, gbc);

            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
            card.add(field, gbc);
        }

        // Button
        actionBtn.setPreferredSize(new Dimension(280, 38));
        gbc.gridx = 0; gbc.gridy = rows.length + 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(18, 8, 0, 8);
        card.add(actionBtn, gbc);

        outer.add(card, new GridBagConstraints());
        return outer;
    }

    // ═══════════════════════  WIDGET HELPERS  ════════════════════════════════

    private JTextField field(String placeholder) {
        JTextField tf = new JTextField(18);
        tf.setFont(FONT_BODY);
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return tf;
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        return cb;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private DefaultTableModel tableModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_BODY);
        t.setRowHeight(26);
        t.getTableHeader().setFont(FONT_BOLD);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        return t;
    }

    private void colorStatusCol(JTable table, int colIndex, String keyword, Color color) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                if (!sel) setForeground(keyword.equals(v) ? color : TEXT_DIM);
                return this;
            }
        });
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void ok(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void bill(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 14, 10, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    private Car findByKey(String key) {
        if (key == null) return null;
        for (Car c : system.getCars())
            if (c.getDisplayKey().equals(key)) return c;
        return null;
    }

    // ═══════════════════════  SETUP  ══════════════════════════════════════════

    public static void setup() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("defaultFont", FONT_BODY);
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
        } catch (Exception ignored) {}
    }
}
