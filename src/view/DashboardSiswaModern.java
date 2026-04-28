package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.sql.*;
import dao.PrestasiDAO; // Menggunakan Kelas DAO
import koneksi.Koneksi;

public class DashboardSiswaModern extends JFrame {

    // --- VARIABEL GLOBAL ---
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentCard = new JPanel(cardLayout);
    private PrestasiDAO dao = new PrestasiDAO();
    private JLabel userLabel = new JLabel("Siswa ");
    private JLabel lblTotalAngka = new JLabel("0");
    private DefaultTableModel modelDashboard;
    private JTable tableDashboard;
    private String currentIdSiswa;
    private FormPrestasi panelInput;
    private JButton btnDashboard, btnInput, btnUpload;
    
    private final Color COLOR_PURPLE = new Color(173, 126, 255); // Ungu pastel kartu
    private final Color COLOR_SIDEBAR = new Color(25, 118, 210); // Biru sidebar
    private final Color COLOR_BG = new Color(245, 247, 251);

    public DashboardSiswaModern(String idSiswa) {
        this.currentIdSiswa = idSiswa;

        setTitle("SISMONPRAS - Dashboard Siswa");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH); 

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(25, 118, 210));
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
        sidebar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel logo = new JLabel("SISMONPRAS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setBorder(new EmptyBorder(10, 0, 30, 0));
        sidebar.add(logo);

        btnDashboard = createSidebarButton("Dashboard", new HomeIcon(), true);
        btnInput = createSidebarButton("Aktivitas Siswa", new ListIcon(), false);
        JButton btnLogout = createSidebarButton("Logout", null, false);

        sidebar.add(btnDashboard);
        sidebar.add(btnInput);
        sidebar.add(btnLogout);

        // --- TOPBAR ---
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setPreferredSize(new Dimension(1200, 70));
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel titleLbl = new JLabel("PORTAL SISWA");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(0, 25, 0, 0));
        Color darkBlueSismon = new Color(23, 70, 122);
        titleLbl.setForeground(darkBlueSismon);
        
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setBorder(new EmptyBorder(0, 0, 0, 25));

        topbar.add(titleLbl, BorderLayout.WEST);
        topbar.add(userLabel, BorderLayout.EAST);

        // --- PENYUSUNAN CARDLAYOUT ---
        mainContentCard.add(createDashboardHome(), "DASHBOARD");
        this.panelInput = new FormPrestasi(currentIdSiswa); // Kirim ID Siswa di sini
        mainContentCard.add(panelInput, "INPUT");

        // --- ASSEMBLE ---
        add(sidebar, BorderLayout.WEST);
        add(topbar, BorderLayout.NORTH);
        add(mainContentCard, BorderLayout.CENTER);

        // --- LOGIKA NAVIGASI & REFRESH ---
        btnDashboard.addActionListener(e -> {
            loadDataDashboard(); // DIPICU SAAT KLIK: Refresh data dari DAO
            updateNavUI(btnDashboard);
            cardLayout.show(mainContentCard, "DASHBOARD");
        });
        btnInput.addActionListener(e -> {
            updateNavUI(btnInput);
            cardLayout.show(mainContentCard, "INPUT");
        });
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin Logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });

        // Initial Load
        loadUserHeader();
        loadDataDashboard();
        
        setVisible(true);
    }

    // --- METHOD DAO: LOAD DATA ---
    // Cari method loadDataDashboard di DashboardSiswaModern.java
    void loadDataDashboard() {
        modelDashboard.setRowCount(0);
        int total = 0;
        try (ResultSet rs = dao.getPrestasiBySiswa(currentIdSiswa)) {
            while (rs != null && rs.next()) {
                total++;
                modelDashboard.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("nama_prestasi"),
                    rs.getString("status")
                });
            }
            lblTotalAngka.setText(String.valueOf(total));
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    private void loadUserHeader() {
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nama_siswa FROM siswa WHERE id_siswa = ?")) {
            ps.setString(1, currentIdSiswa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) userLabel.setText(rs.getString("nama_siswa"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- UI GENERATOR: HALAMAN HOME ---
    private JPanel createDashboardHome() {
        JPanel homePanel = new JPanel(new BorderLayout(0, 25));
        homePanel.setBackground(COLOR_BG);
        homePanel.setBorder(new EmptyBorder(30, 30, 30, 30));

    // Statistik Card Wrapper
        JPanel statsWrapper = new JPanel(new GridLayout(1, 2, 25, 0));
        statsWrapper.setOpaque(false);
        statsWrapper.setPreferredSize(new Dimension(0, 140));
        
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(10, 0, 10, 0));

    // Card 1: Profil (PASTIKAN MENGGUNAKAN createPurpleCard)
        JPanel profileCard = createPurpleCard(); 
        profileCard.add(new JLabel(new UserIcon()), BorderLayout.WEST);
    
        JPanel pText = new JPanel(new GridLayout(2, 1)); 
        pText.setOpaque(false);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        userLabel.setForeground(Color.WHITE); // Teks jadi putih
        pText.add(userLabel);
        JLabel lblAktif = new JLabel("Siswa Aktif");
        lblAktif.setForeground(Color.WHITE);
        lblAktif.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pText.add(lblAktif);
        profileCard.add(pText, BorderLayout.CENTER);

    // Card 2: Total (PASTIKAN MENGGUNAKAN createPurpleCard)
       JPanel totalCard = createPurpleCard();
       totalCard.add(new JLabel(new TrophyIcon()), BorderLayout.WEST);
    
       JPanel tText = new JPanel(new GridLayout(2, 1)); 
       tText.setOpaque(false);
       JLabel tTitle = new JLabel("Total Prestasi Saya");
       tTitle.setForeground(Color.WHITE);
       tTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
       lblTotalAngka.setFont(new Font("Segoe UI", Font.BOLD, 36));
       lblTotalAngka.setForeground(Color.WHITE);
       tText.add(tTitle);
       tText.add(lblTotalAngka);
       totalCard.add(tText, BorderLayout.CENTER);

       statsWrapper.add(profileCard);
       statsWrapper.add(totalCard);
       
       // --- 2. Tabel Section (Dengan Judul) ---
       JPanel tableContainer = new JPanel(new BorderLayout()); // Wadah utama tabel + judul
       tableContainer.setOpaque(false);

        modelDashboard = new DefaultTableModel(new String[]{"ID", "Nama Prestasi", "Status"}, 0);
        tableDashboard = new JTable(modelDashboard);
        tableDashboard.setRowHeight(40);
        
        // Renderer untuk warna status
        tableDashboard.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if(val.equalsIgnoreCase("Valid")) c.setForeground(new Color(46, 125, 50));
                else if(val.equalsIgnoreCase("Pending")) c.setForeground(Color.ORANGE);
                else c.setForeground(Color.RED);
                setHorizontalAlignment(CENTER);
                return c;
            }
        });
    
       tableHeader.setOpaque(false);
       tableHeader.setBorder(new EmptyBorder(0, 5, 10, 0));
       JLabel tableTitle = new JLabel("LINIMASA PRESTASI");
       tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
       tableHeader.add(tableTitle, BorderLayout.WEST);
       Color darkBlueSismon = new Color(23, 70, 122);
       tableTitle.setForeground(darkBlueSismon);
       
        
        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setBackground(Color.WHITE);
        tableSection.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        tableSection.add(new JScrollPane(tableDashboard), BorderLayout.CENTER);
        tableContainer.setOpaque(false);
        tableContainer.add(tableHeader, BorderLayout.NORTH); 
        tableContainer.add(tableSection, BorderLayout.CENTER);
        homePanel.add(statsWrapper, BorderLayout.NORTH);
        homePanel.add(tableContainer, BorderLayout.CENTER);

        return homePanel;
    }

    // --- HELPER UI ---
    private void updateNavUI(JButton active) {
        btnDashboard.setBackground(new Color(25, 118, 210));
        btnInput.setBackground(new Color(25, 118, 210));
        active.setBackground(new Color(21, 101, 192));
    }

    private JPanel createBaseCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        return card;
    }
    private JPanel createPurpleCard() {
    JPanel card = new JPanel(new BorderLayout(25, 0));
    card.setBackground(COLOR_PURPLE); // Menggunakan variabel global Ungu yang sudah kamu buat
    card.setBorder(new EmptyBorder(20, 30, 20, 30));
    return card;
}

    private JButton createSidebarButton(String text, Icon icon, boolean isActive) {
        JButton btn = new JButton(text, icon);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));
        btn.setFocusPainted(false);
        btn.setBackground(isActive ? new Color(21, 101, 192) : new Color(25, 118, 210));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        return btn;
    }

    // --- ICONS ---
    class UserIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, 60, 60); // Lingkaran putih besar
            g2.setColor(new Color(60, 60, 60)); // Icon orang gelap
            g2.fillOval(x + 20, y + 12, 20, 20);
            g2.fillArc(x + 10, y + 35, 40, 30, 0, 180);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 70; }
        @Override public int getIconHeight() { return 65; }
    }
    // --- ICONS: TROPHY IMPROVED ---
    class TrophyIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Warna Gradasi Gold (Cerah ke Gelap)
        GradientPaint goldGradient = new GradientPaint(
            x+30, y+5, new Color(255, 215, 0), // Emas Cerah
            x+30, y+45, new Color(218, 165, 32) // Emas Gelap (Goldenrod)
        );
        g2.setPaint(goldGradient);

        // 1. Badan Utama Piala (Gelas Besar)
        int[] xCup = {x+12, x+48, x+42, x+18}; int[] yCup = {y+5, y+5, y+35, y+35};
        g2.fillPolygon(xCup, yCup, 4);
        g2.fillOval(x+12, y+2, 36, 6); // Tutup atas oval

        // 2. Pegangan Piala (Kanan & Kiri - Efek Outline Realistis)
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(218, 165, 32)); // Warna Emas Outline
        g2.drawArc(x+2, y+10, 15, 15, 90, 180); // Pegangan Kiri
        g2.drawArc(x+43, y+10, 15, 15, 270, 180); // Pegangan Kanan

        // 3. Batang dan Kaki Piala
        g2.setColor(new Color(218, 165, 32));
        g2.fillRoundRect(x+26, y+35, 8, 15, 3, 3); // Batang tengah
        
        // Kaki Membulat
        int[] xBase = {x+18, x+42, x+45, x+15}; int[] yBase = {y+50, y+50, y+60, y+60};
        g2.fillPolygon(xBase, yBase, 4);

        // 4. Tambahkan Efek Kilau (Highlight Putih Transparan)
        g2.setPaint(new Color(255, 255, 255, 100)); // Putih dengan Alpha transparan
        g2.fillOval(x+20, y+8, 10, 20); // Kilau di badan piala

        g2.dispose();
    }
    @Override public int getIconWidth() { return 65; }
    @Override public int getIconHeight() { return 65; }
    }

    class HomeIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(c.getForeground());
            Path2D.Double p = new Path2D.Double();
            p.moveTo(x+10, y+2); p.lineTo(x+2, y+10); p.lineTo(x+5, y+10); p.lineTo(x+5, y+18);
            p.lineTo(x+15, y+18); p.lineTo(x+15, y+10); p.lineTo(x+18, y+10); p.closePath();
            g2.fill(p); g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    class ListIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x+2, y+3, 5, 5); g2.drawRect(x+2, y+11, 5, 5);
            g2.fillRect(x+10, y+5, 8, 2); g2.fillRect(x+10, y+13, 8, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    class UploadIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(c.getForeground());
            g2.fillRect(x+9, y+6, 2, 8);
            g2.fillPolygon(new int[]{x+5, x+10, x+15}, new int[]{y+9, y+4, y+9}, 3);
            g2.fillRect(x+3, y+15, 14, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }
}