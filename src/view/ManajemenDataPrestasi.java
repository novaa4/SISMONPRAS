package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.MessageFormat;

import dao.ManajemenPrestasiDAO;
import model.Prestasi;

public class ManajemenDataPrestasi extends JPanel {

    private final Color primaryBlue = new Color(28, 69, 114);   
    private final Color btnSimpanCol = new Color(22, 160, 201);    
    private final Color btnUpdateCol = new Color(255, 153, 51);    
    private final Color btnHapusCol = new Color(231, 63, 81);      
    private final Color btnExportCol = new Color(119, 136, 153);  
    private final Color btnVerifikasiCol = new Color(40, 167, 69);
    private final Color bgColor = new Color(248, 249, 250);    

    private JTextField idField = new JTextField();
    private JTextField namaField = new JTextField();
    private JTextField siswaField = new JTextField();
    private JTextField searchField = new JTextField();
    private JComboBox<String> tingkatCombo = new JComboBox<>();
    private JComboBox<String> jenisCombo = new JComboBox<>();
    private JComboBox<String> kategoriCombo = new JComboBox<>(new String[]{"Akademik", "Non-Akademik"});

    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Nama Siswa", "Nama Prestasi", "Status", "Kategori","Aksi", "Path"}, 0);
    private JTable table = new JTable(model);
    private ManajemenPrestasiDAO dao = new ManajemenPrestasiDAO();

    public ManajemenDataPrestasi() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // HEADER 
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel lblJudul = new JLabel("MANAJEMEN DATA PRESTASI");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblJudul.setForeground(primaryBlue);
        lblJudul.setBorder(new EmptyBorder(15, 15, 15, 15));
        headerPanel.add(lblJudul);
        add(headerPanel, BorderLayout.NORTH);

        //  CENTER PANEL 
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(bgColor);

        // Form Input
        JPanel formInput = new JPanel(new GridBagLayout());
        formInput.setBackground(bgColor);
        formInput.setBorder(new EmptyBorder(20, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // Baris 1: ID & Kategori
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formInput.add(new JLabel("ID Prestasi") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        formInput.add(idField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        formInput.add(new JLabel("Pilih Tabel") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        formInput.add(kategoriCombo, gbc);

        // Baris 2: Nama
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formInput.add(new JLabel("Nama Prestasi") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formInput.add(namaField, gbc);
        
        // Baris 3: ID Siswa
        gbc.gridx = 0; gbc.gridy = 4; 
        formInput.add(new JLabel("ID Siswa") {{ setFont(labelFont); }}, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        formInput.add(siswaField, gbc); 

        // Baris 4: Tingkat & Jenis
        gbc.gridwidth = 1; gbc.gridy = 2;
        gbc.gridx = 0;
        formInput.add(new JLabel("Tingkat") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1;
        formInput.add(tingkatCombo, gbc);
        gbc.gridx = 2;
        formInput.add(new JLabel("Jenis") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 3;
        formInput.add(jenisCombo, gbc);

        // Baris 5: Cari
        gbc.gridx = 0; gbc.gridy = 3;
        formInput.add(new JLabel("Cari Data") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formInput.add(searchField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        btnPanel.setBackground(bgColor);
        btnPanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        JButton simpan = createBtn("+  SIMPAN", btnSimpanCol);
        JButton update = createBtn("✎  UPDATE", btnUpdateCol);
        JButton verifikasi = createBtn("✓  VERIFIKASI", btnVerifikasiCol);
        JButton hapus = createBtn("🗑  HAPUS", btnHapusCol);
        JButton export = createBtn("👤  EXPORT PDF", btnExportCol);

        btnPanel.add(simpan); btnPanel.add(update); btnPanel.add(verifikasi);
        btnPanel.add(hapus); btnPanel.add(export);

        // Table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 40, 20, 40));

        table.setRowHeight(35);
        styleTableStatus();
        JScrollPane sp = new JScrollPane(table);

        tableContainer.add(new JLabel("Data Prestasi Terdaftar (Akademik & Non-Akademik)") {{ 
            setFont(new Font("Segoe UI", Font.BOLD, 16)); 
            setBorder(new EmptyBorder(0,0,10,0));
        }}, BorderLayout.NORTH);
        tableContainer.add(sp, BorderLayout.CENTER);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(formInput, BorderLayout.NORTH);
        topWrapper.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(topWrapper, BorderLayout.NORTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        //  LISTENERS
        loadTable();
        loadCombo();
        
        simpan.addActionListener(e -> insert());
        update.addActionListener(e -> update());
        verifikasi.addActionListener(e -> verifikasiData());
        hapus.addActionListener(e -> delete());
        export.addActionListener(e -> cetakKePDF());
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { cari(); }
        });
        
        table.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        int r = table.getSelectedRow();
        idField.setText(model.getValueAt(r, 0).toString());
        namaField.setText(model.getValueAt(r, 2).toString());
        kategoriCombo.setSelectedItem(model.getValueAt(r, 4).toString());
        
        idField.setEditable(false); 
    }
    });
    }
    private void styleTableStatus() {

        table.getColumnModel().getColumn(5).setPreferredWidth(250);
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRendererAdmin());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditorAdmin());

        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setWidth(0);

        // RENDERER WARNA STATUS
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(CENTER);
                String status = (value != null) ? value.toString() : "";
                
                if (status.equalsIgnoreCase("Valid")) label.setForeground(new Color(46, 125, 50));
                else if (status.equalsIgnoreCase("Pending")) label.setForeground(new Color(255, 152, 0));
                else if (status.equalsIgnoreCase("Ditolak")) label.setForeground(new Color(211, 47, 47));
                else label.setForeground(Color.BLACK);
                
                return label;
            }
        });
    }
    private void resetForm() {
    idField.setText("");
    namaField.setText("");
    siswaField.setText(""); 
    idField.setEditable(true);
    table.clearSelection();
    }
    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12)); 
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public JPanel getPanel() {
        return this;
    }

    public void loadTable() {
    model.setRowCount(0);
    try (ResultSet rs = dao.getData()) {
        while (rs != null && rs.next()) {
            model.addRow(new Object[]{
                rs.getString("id"),
                rs.getString("nama_siswa"),
                rs.getString("nama_prestasi"),
                rs.getString("status"),
                rs.getString("kategori"), 
                "" ,           
                rs.getString("path")
            });
        }
    } catch (Exception e) { 
        e.printStackTrace(); 
    }
}

    void loadCombo() {
        tingkatCombo.removeAllItems();
        jenisCombo.removeAllItems();
        try {
            ResultSet t = dao.getTingkat();
            while (t != null && t.next()) tingkatCombo.addItem(t.getString("id_tingkat"));
            ResultSet j = dao.getJenis();
            while (j != null && j.next()) jenisCombo.addItem(j.getString("id_jenis"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    void insert() {
    if (idField.getText().isEmpty() || namaField.getText().isEmpty() || siswaField.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "ID Prestasi, Nama, dan ID Siswa harus diisi!");
        return;
    }

    String idSiswa = siswaField.getText(); 
    Prestasi p = new Prestasi(idField.getText(), namaField.getText(), idSiswa);

    String kat = kategoriCombo.getSelectedItem().toString();
    dao.insert(p, tingkatCombo.getSelectedItem().toString(), jenisCombo.getSelectedItem().toString(), kat);
    loadTable();
    resetForm();
    }

    void update() {
        if (table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data di tabel!");
            return;
        }
        Prestasi p = new Prestasi(idField.getText(), namaField.getText(), "ADMIN_INPUT");
        String kat = kategoriCombo.getSelectedItem().toString();
        dao.update(p, tingkatCombo.getSelectedItem().toString(), jenisCombo.getSelectedItem().toString(), kat);
        loadTable();
        resetForm();
    }

    void verifikasiData() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data!");
            return;
        }
        String id = model.getValueAt(r, 0).toString();
        String kat = model.getValueAt(r, 3).toString();

        String[] options = {"Validasi", "Tolak", "Batal"};
        int choice = JOptionPane.showOptionDialog(this, "Verifikasi ID: " + id, "Verifikasi",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) dao.updateStatus(id, "Valid", kat);
        else if (choice == 1) dao.updateStatus(id, "Ditolak", kat);
        
        loadTable();
        resetForm();
    }

    void delete() {
    int r = table.getSelectedRow();
    if (r == -1) return;

    String id = model.getValueAt(r, 0).toString().trim();
    String kat = model.getValueAt(r, 4).toString().trim(); 

    int confirm = JOptionPane.showConfirmDialog(this, "Hapus data " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        dao.delete(id, kat);
        loadTable();
    }
}

    void cari() {
        model.setRowCount(0);
        try (ResultSet rs = dao.search(searchField.getText())) {
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("nama_prestasi"),
                    rs.getString("status"),
                    "Hasil Cari"
                });
            }
        } catch (Exception e) { e.printStackTrace(); } // MODUL 7: Exception Handling
    }

    void cetakKePDF() {
        try {
            MessageFormat h = new MessageFormat("LAPORAN DATA PRESTASI SISMONPRAS");
            table.print(JTable.PrintMode.FIT_WIDTH, h, new MessageFormat("Halaman {0}"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    class PanelAksiAdmin extends JPanel {
        JButton btnPreview = new JButton("👁");
        JButton btnSetuju = new JButton("✔");
        JButton btnTolak = new JButton("✘");

    public PanelAksiAdmin() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(true);
        btnPreview.setBackground(new Color(0, 123, 255)); btnPreview.setForeground(Color.WHITE);
        btnSetuju.setBackground(new Color(40, 167, 69)); btnSetuju.setForeground(Color.WHITE);
        btnTolak.setBackground(new Color(220, 53, 69)); btnTolak.setForeground(Color.WHITE);
        add(btnPreview); add(btnSetuju); add(btnTolak);
    }
    }

    class ActionRendererAdmin implements javax.swing.table.TableCellRenderer {
    @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            PanelAksiAdmin p = new PanelAksiAdmin();
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return p;
       }
    }

    class ActionEditorAdmin extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private PanelAksiAdmin panel = new PanelAksiAdmin();

    public ActionEditorAdmin() {
        panel.btnPreview.addActionListener(e -> {
            stopCellEditing();
            int r = table.getSelectedRow();
            String path = model.getValueAt(r, 6).toString();
            try {
                Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Sertifikat belum diunggah atau file tidak ditemukan!");
            }
        });

        panel.btnSetuju.addActionListener(e -> {
            stopCellEditing();
            int r = table.getSelectedRow();
            dao.updateStatus(model.getValueAt(r, 0).toString(), "Valid", model.getValueAt(r, 4).toString());
            loadTable();
        });

        panel.btnTolak.addActionListener(e -> {
            stopCellEditing();
            int r = table.getSelectedRow();
            dao.updateStatus(model.getValueAt(r, 0).toString(), "Ditolak", model.getValueAt(r, 4).toString());
            loadTable();
        });
    }

    @Override public Object getCellEditorValue() { return ""; }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }
}
}
