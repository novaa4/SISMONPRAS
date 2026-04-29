package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;
import koneksi.Koneksi;

public class UploadForm extends JFrame{

    private String path = "";
    private JLabel labelStatus;
    private final Color BLUE_PRIMARY = new Color(25, 118, 210);
    private final Color SIDEBAR_BLUE = new Color(30, 100, 190);
    private final Color BG_LIGHT = new Color(240, 242, 245);
    private String idPrestasi;
    private String kategori;
    

   public UploadForm(String id, String kategori) {
        this.idPrestasi = id;
        this.kategori = kategori;

        setTitle("Upload Sertifikat - " + id);
        setSize(500, 600);
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_LIGHT);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel headerLabel = new JLabel("UPLOAD BUKTI: " + id);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        Color darkBlueSismon = new Color(23, 70, 122);
        headerLabel.setForeground(darkBlueSismon);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        //  DROPZONE 
        JPanel dropZone = new JPanel(new GridBagLayout());
        dropZone.setBackground(Color.WHITE);
        dropZone.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 0, 20, 0),
            BorderFactory.createDashedBorder(Color.GRAY, 5, 5)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel iconCloud = new JLabel("☁"); 
        iconCloud.setFont(new Font("SansSerif", Font.PLAIN, 70));
        iconCloud.setForeground(new Color(200, 200, 200));

        JLabel textDropzone = new JLabel("UPLOAD AREA");
        textDropzone.setFont(new Font("SansSerif", Font.BOLD, 18));
        textDropzone.setForeground(darkBlueSismon);

        JButton btnPilih = new JButton("Unggah File");
        btnPilih.setBackground(BLUE_PRIMARY);
        btnPilih.setForeground(Color.WHITE);
        btnPilih.setFocusPainted(false);
        btnPilih.setPreferredSize(new Dimension(150, 40));
        btnPilih.setFocusPainted(false);

        JLabel textInstruction = new JLabel("Pilih file sertifikat kamu di bawah ini");
        textInstruction.setForeground(Color.GRAY);
        textInstruction.setFont(new Font("SansSerif", Font.PLAIN, 12));

        //Panel Ikon (Baris PDF, JPG, PNG)
        JPanel panelIkon = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelIkon.setBackground(Color.WHITE);
        panelIkon.add(createFileIcon("PDF", new Color(220, 53, 69))); // Merah
        panelIkon.add(createFileIcon("JPG", new Color(40, 167, 69)));  // Hijau
        panelIkon.add(createFileIcon("PNG", new Color(0, 123, 255)));  // Biru

        JLabel labelLimit = new JLabel("Maksimal 2MB. Hanya format yang diizinkan.");
        labelLimit.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelLimit.setForeground(Color.GRAY);
     
        labelStatus = new JLabel("Pilih file sertifikat untuk kategori: " + kategori);
        labelStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelStatus.setForeground(Color.GRAY);

        gbc.gridy = 0; dropZone.add(iconCloud, gbc);
        gbc.gridy = 1; dropZone.add(textDropzone, gbc);
        gbc.gridy = 2; dropZone.add(textInstruction, gbc); // Baru
        gbc.gridy = 3; dropZone.add(btnPilih, gbc);
        gbc.gridy = 4; dropZone.add(panelIkon, gbc);       // Baru
        gbc.gridy = 5; dropZone.add(labelLimit, gbc);      // Baru
        gbc.gridy = 6; dropZone.add(labelStatus, gbc);     // Nama file terpilih

        mainPanel.add(dropZone, BorderLayout.CENTER);

        JButton btnSimpan = new JButton("KONFIRMASI DAN SIMPAN");
        btnSimpan.setPreferredSize(new Dimension(0, 50));
        btnSimpan.setBackground(new Color(40, 167, 69));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("SansSerif", Font.BOLD, 14));
        mainPanel.add(btnSimpan, BorderLayout.SOUTH);

        //  LISTENERS 
        btnPilih.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile().getAbsolutePath();
                labelStatus.setText("Terpilih: " + fc.getSelectedFile().getName());
                labelStatus.setForeground(new Color(40, 167, 69));
            }
        });

        btnSimpan.addActionListener(e -> simpanKeDatabase());

        add(mainPanel, BorderLayout.CENTER);
    }
   
   private JLabel createFileIcon(String ekstensi, Color warnaBg) {
        JLabel label = new JLabel(ekstensi);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(Color.WHITE);
        label.setBackground(warnaBg);
        label.setOpaque(true); 
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(5, 10, 5, 10)); 
        return label;
    }

    private void simpanKeDatabase() {
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih file terlebih dahulu!");
            return;
        }

        // Kolom Foreign Key berdasarkan kategori (Tabel Sertifikat)
        String kolomFK = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";
        
        // QUERY: Masuk ke tabel sertifikat
        String sql = "INSERT INTO sertifikat (id_sertifikat, " + kolomFK + ", nama_file, tanggal_terbit) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE nama_file = VALUES(nama_file)";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, "CERT-" + idPrestasi); // ID Sertifikat unik
            ps.setString(2, idPrestasi);
            ps.setString(3, path); // Menyimpan path file
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sertifikat Berhasil Diupload!");
            this.dispose(); 
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + ex.getMessage()); // MODUL 7: Exception Handling
        }
    }
}