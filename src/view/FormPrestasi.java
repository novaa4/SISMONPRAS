package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.MessageFormat;

import dao.PrestasiDAO;
import model.Prestasi;

public class FormPrestasi extends JPanel {

    private final Color primaryBlue = new Color(28, 69, 114);   
    private final Color btnSimpanCol = new Color(22, 160, 201);    
    private final Color btnUpdateCol = new Color(255, 153, 51);    
    private final Color btnHapusCol = new Color(231, 63, 81);  
    private final Color btnUploadCol = new Color(153, 102, 255);
    private final Color btnExportCol = new Color(119, 136, 153);  
    private final Color bgColor = new Color(248, 249, 250);    

    private JTextField idField = new JTextField();
    private JTextField namaField = new JTextField();
    private JTextField searchField = new JTextField();
    private JComboBox<String> tingkatCombo = new JComboBox<>();
    private JComboBox<String> jenisCombo = new JComboBox<>();
    private JComboBox<String> kategoriCombo = new JComboBox<>(new String[]{"Akademik", "Non-Akademik"});

    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Nama Prestasi", "Status", "Kategori","Aksi","Path"}, 0);
    private JTable table = new JTable(model);
    private PrestasiDAO dao = new PrestasiDAO();
    
    // Variabel untuk menampung ID Siswa yang sedang login 
    private String currentIdSiswa = "3290"; 

    public FormPrestasi(String idSiswa) {
        this.currentIdSiswa = idSiswa;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel lblJudul = new JLabel("FORM INPUT PRESTASI SISWA");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblJudul.setForeground(primaryBlue);
        lblJudul.setBorder(new EmptyBorder(15, 15, 15, 15));
        headerPanel.add(lblJudul);
        add(headerPanel, BorderLayout.NORTH);

        // CENTER PANEL 
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(bgColor);

        // Form Input
        JPanel formInput = new JPanel(new GridBagLayout());
        formInput.setBackground(bgColor);
        formInput.setBorder(new EmptyBorder(20, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // Baris 1: ID & Kategori
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        formInput.add(new JLabel("ID Prestasi") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        formInput.add(idField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.1;
        formInput.add(new JLabel("Kategori") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        formInput.add(kategoriCombo, gbc);

        // Baris 2: Nama
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1; gbc.gridwidth = 1;
        formInput.add(new JLabel("Nama Prestasi") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formInput.add(namaField, gbc);

        // Baris 3: Tingkat & Jenis
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formInput.add(new JLabel("Tingkat") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1;
        formInput.add(tingkatCombo, gbc);
        gbc.gridx = 2;
        formInput.add(new JLabel("Jenis") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 3;
        formInput.add(jenisCombo, gbc);

        // Baris 4: Cari
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formInput.add(new JLabel("Cari Data") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formInput.add(searchField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        btnPanel.setBackground(bgColor);
        btnPanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        JButton simpan = createBtn("+  SIMPAN", btnSimpanCol);
        JButton update = createBtn("✎  UPDATE", btnUpdateCol);
        JButton hapus = createBtn("🗑  HAPUS", btnHapusCol);
        JButton upload = createBtn("⬆  UPLOAD", btnUploadCol);
        JButton export = createBtn("👤  EXPORT PDF", btnExportCol);

        btnPanel.add(simpan); 
        btnPanel.add(update); 
        btnPanel.add(hapus); 
        btnPanel.add(upload);
        btnPanel.add(export);

        // Table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 40, 20, 40));

        table.setRowHeight(35);
        styleTableStatus(); 
        JScrollPane sp = new JScrollPane(table);

        tableContainer.add(new JLabel("Data Prestasi Saya") {{ 
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

        // LISTENERS 
        loadTable();
        loadCombo();
        
        simpan.addActionListener(e -> insert());
        update.addActionListener(e -> update());
        hapus.addActionListener(e -> delete());
        export.addActionListener(e -> cetakKePDF());
        upload.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = model.getValueAt(r, 0).toString();
                String kat = model.getValueAt(r, 3).toString();
                new UploadForm(id, kat).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih data di tabel terlebih dahulu!");
            }
        });
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { cari(); }
        });
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r != -1) {
                    idField.setText(model.getValueAt(r, 0).toString());
                    namaField.setText(model.getValueAt(r, 1).toString());
                    kategoriCombo.setSelectedItem(model.getValueAt(r, 3).toString());
                    idField.setEditable(false);
                }
            }
        });
    }
    // LOGIKA DATA 
    public void loadTable() {
    model.setRowCount(0);
    try (ResultSet rs = dao.getPrestasiBySiswa(currentIdSiswa)) {
        while (rs != null && rs.next()) {
            model.addRow(new Object[]{
                rs.getString("id"),     
                rs.getString("nama_prestasi"),   
                rs.getString("status"),          
                rs.getString("kategori"),        
                "" ,   
                rs.getString("file_path")
            });
        }
    } catch (Exception e) { // MODUL 7: Exception Handling
        e.printStackTrace(); 
        JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }
    
    private void styleTableStatus() {
    table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(CENTER);
            
            String status = (value != null) ? value.toString() : "";
       
            if (status.equalsIgnoreCase("Valid")) {
                label.setForeground(new Color(46, 125, 50)); // Hijau
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (status.equalsIgnoreCase("Pending")) {
                label.setForeground(new Color(255, 152, 0)); // Oranye
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (status.equalsIgnoreCase("Ditolak")) {
                label.setForeground(new Color(211, 47, 47)); // Merah
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                label.setForeground(Color.BLACK);
            }
            
            return label;
        }
    });
          table.getColumnModel().getColumn(4).setPreferredWidth(250);
          table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
          table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());

          table.getColumnModel().getColumn(5).setMinWidth(0);
          table.getColumnModel().getColumn(5).setMaxWidth(0);
          table.getColumnModel().getColumn(5).setWidth(0);

          table.setRowHeight(50);
    }
    private void loadCombo() {
        tingkatCombo.removeAllItems();
        jenisCombo.removeAllItems();
        try {
            ResultSet t = dao.getTingkat();
            while (t != null && t.next()) tingkatCombo.addItem(t.getString("id_tingkat"));
            ResultSet j = dao.getJenis();
            while (j != null && j.next()) jenisCombo.addItem(j.getString("id_jenis"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void resetForm() {
        idField.setText(""); namaField.setText("");
        idField.setEditable(true); table.clearSelection();
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12)); 
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    void insert() {
        if (idField.getText().isEmpty() || namaField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }
        Prestasi p = new Prestasi(idField.getText(), namaField.getText(), currentIdSiswa); // MODUL 1&2: Object 
        String kat = kategoriCombo.getSelectedItem().toString();
        String pathSertif = "";
        dao.insertWithSertifikat(p, tingkatCombo.getSelectedItem().toString(), jenisCombo.getSelectedItem().toString(), kat,pathSertif);
        loadTable();
        resetForm();
    }

    void update() {
    if (table.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data di tabel!");
        return;
    }
    
    Prestasi p = new Prestasi(idField.getText(), namaField.getText(), currentIdSiswa);
    String kat = kategoriCombo.getSelectedItem().toString();
  
    dao.update(p, tingkatCombo.getSelectedItem().toString(), jenisCombo.getSelectedItem().toString(), kat, "");
    
    loadTable();
    resetForm();
    }

    void delete() {
    int r = table.getSelectedRow();
    if (r == -1) return;

    String id = model.getValueAt(r, 0).toString().trim();
    String kat = model.getValueAt(r, 4).toString().trim(); 

    if (JOptionPane.showConfirmDialog(this, "Hapus prestasi: " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == 0) {
        dao.delete(id, kat);
        loadTable(); 
    }
}

    void cari() {
        model.setRowCount(0);
        try (ResultSet rs = dao.search(searchField.getText())) {
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{ rs.getString("id"), rs.getString("nama_prestasi"), rs.getString("status"), "Hasil Cari" });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void cetakKePDF() {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("LAPORAN PRESTASI"), new MessageFormat("Hal {0}"));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    //  RENDERER UNTUK MENAMPILKAN TOMBOL 
    class ActionPanelRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        public ActionPanelRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(true);
        
        JButton edit = new JButton("✎");
        edit.setBackground(new Color(255, 193, 7)); // Warna Kuning (Gambar)
        
        JButton hapus = new JButton("🗑");
        hapus.setBackground(new Color(220, 53, 69)); // Warna Merah (Gambar)
        
        JButton file = new JButton("File Dukung ▼");
        file.setBackground(new Color(0, 123, 255)); // Warna Biru (Gambar)
        file.setForeground(Color.WHITE);

        add(edit); add(hapus); add(file);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return this;
    }
}

// EDITOR UNTUK MENANGANI KLIK TOMBOL 
    class ActionPanelEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected JButton btnFile;

    public ActionPanelEditor(JTextField textField) {
        super(textField);
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JButton edit = new JButton("✎");
        JButton hapus = new JButton("🗑");
        btnFile = new JButton("File Dukung ▼");

        // Logika Dropdown "File Dukung"
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("📄 Scan Sertifikat"));
        btnFile.addActionListener(e -> {
            menu.show(btnFile, 0, btnFile.getHeight());
        });

        // Logika Hapus 
        hapus.addActionListener(e -> delete());

        panel.add(edit); panel.add(hapus); panel.add(btnFile);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return panel;
    }

    @Override
    public Object getCellEditorValue() { return ""; }
    }
    //  CLASS TAMPILAN TOMBOL 
    class PanelAksi extends JPanel {
    JButton btnEdit = new JButton("✎");
    JButton btnHapus = new JButton("🗑");
    JButton btnFile = new JButton("File Dukung ▼");

    public PanelAksi() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        setOpaque(true);
        btnEdit.setBackground(new Color(255, 193, 7));
        btnHapus.setBackground(new Color(255, 82, 82));
        btnFile.setBackground(new Color(13, 110, 253));
        btnFile.setForeground(Color.WHITE);
        add(btnEdit); add(btnHapus); add(btnFile);
    }
}

    // CLASS RENDERER 
    class ActionRenderer implements javax.swing.table.TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        PanelAksi p = new PanelAksi();
        Object pathObj = table.getValueAt(row, 5);
        String path = (pathObj != null) ? pathObj.toString() : "";

        if (path.isEmpty() || path.equals("null")) {
            p.btnFile.setText("No File");
            p.btnFile.setBackground(Color.LIGHT_GRAY); 
        } else {
            p.btnFile.setText("Lihat File");
            p.btnFile.setBackground(new Color(13, 110, 253)); 
        }
        
        p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return p;
    }
}

// CLASS EDITOR 
    class ActionEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private PanelAksi panel = new PanelAksi();

        public ActionEditor() {
            // Logika Lihat File
            panel.btnFile.addActionListener(e -> {
                stopCellEditing();
                int row = table.getSelectedRow();
                Object pathObj = table.getValueAt(row, 5);
                String path = (pathObj != null) ? pathObj.toString() : "";

                if (!path.isEmpty() && !path.equals("null")) {
                    try {
                        Desktop.getDesktop().open(new java.io.File(path));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Gagal membuka file!"); // MODUL 7: Exception Handling
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "File belum diupload!");
                }
            });

        // 2. LOGIKA TOMBOL HAPUS (MERAH)
        panel.btnHapus.addActionListener(e -> {
            stopCellEditing();
            delete(); // Memanggil method delete() yang ada di FormPrestasi
        });

        // 3. LOGIKA TOMBOL EDIT (KUNING)
        panel.btnEdit.addActionListener(e -> {
            stopCellEditing();
            int row = table.getSelectedRow();
            if (row != -1) {
                // Memindahkan data tabel kembali ke field input
                idField.setText(table.getValueAt(row, 0).toString());
                namaField.setText(table.getValueAt(row, 1).toString());
                kategoriCombo.setSelectedItem(table.getValueAt(row, 3).toString());
                idField.setEditable(false);
            }
        });
    }
   
    private void bukaFormUpload(String id, String kat) {
        UploadForm fUpload = new UploadForm(id, kat);
        fUpload.setVisible(true);

        fUpload.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadTable();
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }
}
}