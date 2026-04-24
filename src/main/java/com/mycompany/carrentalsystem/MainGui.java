package com.mycompany.carrentalsystem;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class MainGui extends JFrame {

    // ── Palette (dark theme accents) ─────────────────────────────────────────
    private static final Color ACCENT       = new Color(99, 102, 241);   // indigo
    private static final Color ACCENT_HOVER = new Color(79,  70, 229);
    private static final Color SUCCESS      = new Color(34, 197,  94);
    private static final Color DANGER       = new Color(239,  68,  68);
    private static final Color WARNING      = new Color(251, 191,  36);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Font  FONT_UI      = new Font("Segoe UI", Font.PLAIN,  13);
    private static final Font  FONT_BOLD    = new Font("Segoe UI", Font.BOLD,   13);
    private static final Font  FONT_HEADER  = new Font("Segoe UI", Font.BOLD,   22);
    private static final Font  FONT_SUB     = new Font("Segoe UI", Font.PLAIN,  12);

    // ── Service ───────────────────────────────────────────────────────────────
    private final RentalSystem system = new RentalSystem();

    // ── Add-Car ───────────────────────────────────────────────────────────────
    private JTextField tfBrand, tfModel, tfPlate, tfPrice;
    private JComboBox<String> cbCarType;

    // ── Rent-Car ──────────────────────────────────────────────────────────────
    private JComboBox<String> cbRentType, cbRentCar;
    private JTextField tfCustName, tfCustPhone, tfCustCnic;
    private JSpinner   spDays;

    // ── Return-Car ────────────────────────────────────────────────────────────
    private JComboBox<String> cbReturnCar;

    // ── View-Cars ─────────────────────────────────────────────────────────────
    private DefaultTableModel tmAvailable, tmRented;
    private JTextField tfSearch;

    // ── History ───────────────────────────────────────────────────────────────
    private DefaultTableModel tmHistory;

    // ─────────────────────────────────────────────────────────────────────────

    public MainGui() {
        setTitle("Car Rental System");
        setSize(880, 600);
        setMinimumSize(new Dimension(780, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Root layout ───────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);

        refreshAll();
    }

    // ══════════════════════════  HEADER  ═════════════════════════════════════

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(16, 28, 16, 28));

        JLabel title = new JLabel("  Car Rental System");
        title.setFont(FONT_HEADER);
        title.setForeground(Color.WHITE);
        title.setIcon(colorIcon(Color.WHITE, 28));   // small circle icon

        JLabel sub = new JLabel("Manage your fleet · Track rentals · View history");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(199, 210, 254));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        // Stats badge on the right
        JLabel stats = new JLabel();
        stats.setFont(FONT_BOLD);
        stats.setForeground(new Color(224, 231, 255));
        updateStatsBadge(stats);
        this.statsBadge = stats;

        header.add(left,  BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JLabel statsBadge;

    private void updateStatsBadge(JLabel label) {
        int total    = system.getCars().size();
        int available = (int) system.getCars().stream().filter(Car::isAvailable).count();
        int rented   = total - available;
        label.setText(String.format("Total: %d   Available: %d   Rented: %d", total, available, rented));
    }

    // ══════════════════════════  TABS  ═══════════════════════════════════════

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(FONT_BOLD);
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE,
                FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_PREFERRED);
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 44);

        tabs.addTab("  Add Car  ",     buildAddCarPanel());
        tabs.addTab("  Rent Car  ",    buildRentCarPanel());
        tabs.addTab("  Return Car  ",  buildReturnCarPanel());
        tabs.addTab("  View Cars  ",   buildViewCarsPanel());
        tabs.addTab("  History  ",     buildHistoryPanel());
        return tabs;
    }

    // ═══════════════════  TAB 1 — ADD CAR  ═══════════════════════════════════

    private JPanel buildAddCarPanel() {
        JPanel wrap = centeredCard();

        tfBrand   = roundedField();
        tfModel   = roundedField();
        tfPlate   = roundedField();
        tfPrice   = roundedField();
        cbCarType = styledCombo(new String[]{"Economy", "Luxury", "SUV"});

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 14));
        form.setOpaque(false);
        form.add(label("Brand"));          form.add(tfBrand);
        form.add(label("Model"));          form.add(tfModel);
        form.add(label("Plate Number"));   form.add(tfPlate);
        form.add(label("Price / Day (Rs)")); form.add(tfPrice);
        form.add(label("Car Type"));       form.add(cbCarType);

        JButton btn = accentButton("Add Car", ACCENT);
        btn.addActionListener(e -> onAddCar());

        JPanel card = card("Add New Car");
        card.add(form, BorderLayout.CENTER);
        card.add(btn,  BorderLayout.SOUTH);

        wrap.add(card);
        return wrap;
    }

    private void onAddCar() {
        String brand = tfBrand.getText().trim();
        String model = tfModel.getText().trim();
        String plate = tfPlate.getText().trim();
        String priceText = tfPrice.getText().trim();

        if (brand.isEmpty() || model.isEmpty() || plate.isEmpty() || priceText.isEmpty()) {
            showError("Please fill in all fields."); return;
        }
        if (system.plateExists(plate)) {
            showError("Plate \"" + plate + "\" already exists."); return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Enter a valid positive number for Price/Day."); return;
        }

        String type = (String) cbCarType.getSelectedItem();
        Car car = switch (type) {
            case "Luxury" -> new LuxuryCar(brand, model, plate, price);
            case "SUV"    -> new SUVCar(brand, model, plate, price);
            default       -> new EconomyCar(brand, model, plate, price);
        };
        system.addCar(car);
        showSuccess("Car added: " + car.getDisplayKey());
        tfBrand.setText(""); tfModel.setText(""); tfPlate.setText(""); tfPrice.setText("");
        refreshAll();
    }

    // ═══════════════════  TAB 2 — RENT CAR  ══════════════════════════════════

    private JPanel buildRentCarPanel() {
        JPanel wrap = centeredCard();

        cbRentType = styledCombo(new String[]{"Economy", "Luxury", "SUV"});
        cbRentType.addActionListener(e -> refreshRentCarBox());
        cbRentCar  = styledCombo(new String[]{});

        tfCustName  = roundedField();
        tfCustPhone = roundedField();
        tfCustCnic  = roundedField();
        tfCustCnic.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. 35202-1234567-1");

        spDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spDays.setFont(FONT_UI);
        ((JSpinner.DefaultEditor) spDays.getEditor()).getTextField()
                .putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 80);

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 14));
        form.setOpaque(false);
        form.add(label("Car Type"));       form.add(cbRentType);
        form.add(label("Select Car"));     form.add(cbRentCar);
        form.add(label("Rental Days"));    form.add(spDays);
        form.add(label("Customer Name"));  form.add(tfCustName);
        form.add(label("Phone"));          form.add(tfCustPhone);
        form.add(label("CNIC"));           form.add(tfCustCnic);

        JButton btn = accentButton("Rent & Generate Bill", SUCCESS);
        btn.addActionListener(e -> onRentCar());

        JPanel card = card("Rent a Car");
        card.add(form, BorderLayout.CENTER);
        card.add(btn,  BorderLayout.SOUTH);

        wrap.add(card);
        return wrap;
    }

    private void refreshRentCarBox() {
        cbRentCar.removeAllItems();
        String type = (String) cbRentType.getSelectedItem();
        for (Car c : system.getCars())
            if (c.isAvailable() && c.getCarType().equals(type))
                cbRentCar.addItem(c.getDisplayKey());
    }

    private void onRentCar() {
        if (cbRentCar.getItemCount() == 0) { showError("No available cars of this type."); return; }
        String name  = tfCustName.getText().trim();
        String phone = tfCustPhone.getText().trim();
        String cnic  = tfCustCnic.getText().trim();
        int days = (int) spDays.getValue();
        if (name.isEmpty() || phone.isEmpty() || cnic.isEmpty()) { showError("Fill in all customer details."); return; }

        Car car = findCarByKey((String) cbRentCar.getSelectedItem());
        if (car == null) { showError("Car not found."); return; }

        RentalRecord rec = system.rentCar(car, new Customer(name, phone, cnic), days);
        if (rec == null) { showError("Car is no longer available."); return; }

        showBill("Rental Bill", rec.getBillSummary());
        tfCustName.setText(""); tfCustPhone.setText(""); tfCustCnic.setText("");
        spDays.setValue(1);
        refreshAll();
    }

    // ═══════════════════  TAB 3 — RETURN CAR  ════════════════════════════════

    private JPanel buildReturnCarPanel() {
        JPanel wrap = centeredCard();

        cbReturnCar = styledCombo(new String[]{});

        JPanel form = new JPanel(new GridLayout(1, 2, 12, 14));
        form.setOpaque(false);
        form.add(label("Select Rented Car")); form.add(cbReturnCar);

        JButton btn = accentButton("Return Car", DANGER);
        btn.addActionListener(e -> onReturnCar());

        JPanel card = card("Return a Car");
        card.add(form, BorderLayout.CENTER);
        card.add(btn,  BorderLayout.SOUTH);

        wrap.add(card);
        return wrap;
    }

    private void onReturnCar() {
        if (cbReturnCar.getItemCount() == 0) { showError("No cars are currently rented."); return; }
        Car car = findCarByKey((String) cbReturnCar.getSelectedItem());
        if (car == null) { showError("Car not found."); return; }

        RentalRecord rec = system.returnCar(car);
        String msg = rec != null
            ? rec.getBillSummary() + "\n─────────────────────────────────\nReturn Date : " + rec.getReturnDateStr()
            : "Car returned successfully.";
        showBill("Final Bill", msg);
        refreshAll();
    }

    // ═══════════════════  TAB 4 — VIEW CARS  ══════════════════════════════════

    private JPanel buildViewCarsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search + Remove bar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(FONT_BOLD);
        tfSearch = roundedField();
        tfSearch.setPreferredSize(new Dimension(220, 32));
        tfSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Brand, model, plate...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTables(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTables(); }
        });

        JButton btnRemove = accentButton("Remove Car", DANGER);
        btnRemove.setPreferredSize(new Dimension(130, 32));
        btnRemove.addActionListener(e -> onRemoveCar());

        bar.add(searchIcon); bar.add(tfSearch); bar.add(Box.createHorizontalStrut(8)); bar.add(btnRemove);

        String[] cols = {"Brand", "Model", "Plate", "Type", "Price/Day (Rs)", "Status"};
        tmAvailable = noEditModel(cols);
        tmRented    = noEditModel(cols);

        JTable tblAvail  = styledTable(tmAvailable, true);
        JTable tblRented = styledTable(tmRented, false);

        JScrollPane spAvail  = scrollPane(tblAvail,  "Available Cars");
        JScrollPane spRented = scrollPane(tblRented, "Rented Cars");

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spAvail, spRented);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setDividerSize(6);

        panel.add(bar,   BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void onRemoveCar() {
        List<Car> avail = system.getAvailableCars();
        if (avail.isEmpty()) { showError("No available cars to remove."); return; }
        String[] opts = avail.stream().map(Car::getDisplayKey).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this, "Select car to remove:",
                "Remove Car", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (chosen == null) return;
        Car car = findCarByKey(chosen);
        if (car != null && system.removeCar(car)) {
            showSuccess("Removed: " + chosen);
            refreshAll();
        } else {
            showError("Cannot remove a currently rented car.");
        }
    }

    private void filterTables() {
        updateViewTables(tfSearch.getText().trim().toLowerCase());
    }

    // ═══════════════════  TAB 5 — HISTORY  ════════════════════════════════════

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Customer", "Phone", "Car", "Type", "Days", "Total (Rs)", "Rented On", "Returned On", "Status"};
        tmHistory = noEditModel(cols);
        JTable tbl = styledTable(tmHistory, true);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tmHistory);
        tbl.setRowSorter(sorter);

        // Status column coloring
        tbl.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                String val = v == null ? "" : v.toString();
                if (!sel) setForeground("Active".equals(val) ? SUCCESS : TEXT_MUTED);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(titledBorder("All Rental Transactions (click column header to sort)"));

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════  REFRESH HELPERS  ════════════════════════════════

    private void refreshAll() {
        updateViewTables("");
        refreshRentCarBox();
        cbReturnCar.removeAllItems();
        for (Car c : system.getRentedCars()) cbReturnCar.addItem(c.getDisplayKey());
        updateHistoryTable();
        if (tfSearch != null) tfSearch.setText("");
        if (statsBadge != null) updateStatsBadge(statsBadge);
    }

    private void updateViewTables(String q) {
        tmAvailable.setRowCount(0);
        tmRented.setRowCount(0);
        for (Car c : system.getCars()) {
            boolean matches = q.isEmpty()
                || c.getBrand().toLowerCase().contains(q)
                || c.getModel().toLowerCase().contains(q)
                || c.getPlateNumber().toLowerCase().contains(q)
                || c.getCarType().toLowerCase().contains(q);
            if (!matches) continue;
            Object[] row = {c.getBrand(), c.getModel(), c.getPlateNumber(),
                            c.getCarType(), String.format("%.0f", c.getPricePerDay()),
                            c.isAvailable() ? "Available" : "Rented"};
            if (c.isAvailable()) tmAvailable.addRow(row);
            else                 tmRented.addRow(row);
        }
    }

    private void updateHistoryTable() {
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

    // ══════════════════════  UI HELPERS  ═════════════════════════════════════

    /** Centered wrapper so the card stays compact in wide windows. */
    private JPanel centeredCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        return wrap;
    }

    /** White rounded card with a section title. */
    private JPanel card(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setPreferredSize(new Dimension(460, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JPanel sep = new JPanel(new BorderLayout(0, 8));
        sep.setOpaque(false);
        sep.add(lbl, BorderLayout.NORTH);
        sep.add(new JSeparator(), BorderLayout.SOUTH);

        card.add(sep, BorderLayout.NORTH);
        return card;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        return l;
    }

    private JTextField roundedField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_UI);
        tf.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 180);
        return tf;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_UI);
        return cb;
    }

    private JButton accentButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(200, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    private JTable styledTable(DefaultTableModel model, boolean stripedAvail) {
        JTable t = new JTable(model);
        t.setFont(FONT_UI);
        t.setRowHeight(28);
        t.getTableHeader().setFont(FONT_BOLD);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.putClientProperty(FlatClientProperties.STYLE,
                "showHorizontalLines: true; alternateRowColor: $Table.alternateRowBackground");
        return t;
    }

    private JScrollPane scrollPane(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(titledBorder(title));
        return sp;
    }

    private javax.swing.border.Border titledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
            title, javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP, FONT_BOLD);
    }

    private DefaultTableModel noEditModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showBill(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(12, 16, 12, 16));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    private Car findCarByKey(String key) {
        if (key == null) return null;
        for (Car c : system.getCars())
            if (c.getDisplayKey().equals(key)) return c;
        return null;
    }

    /** Tiny filled circle icon used in the header. */
    private Icon colorIcon(Color color, int size) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(x, y, size - 4, size - 4);
                g2.dispose();
            }
            public int getIconWidth()  { return size; }
            public int getIconHeight() { return size; }
        };
    }

    // ══════════════════════  ENTRY POINT  ════════════════════════════════════

    public static void setup() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("defaultFont", FONT_UI);
            UIManager.put("TabbedPane.tabInsets", new Insets(6, 16, 6, 16));
            UIManager.put("TabbedPane.selectedBackground", ACCENT);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.alternateRowBackground", new Color(30, 33, 48));
        } catch (Exception ignored) {}
    }
}
