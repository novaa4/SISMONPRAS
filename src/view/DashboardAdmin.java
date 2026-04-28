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
import java.sql.ResultSet;
import dao.ManajemenPrestasiDAO; // Pastikan import ini ada

public class DashboardAdmin extends JFrame {

    // --- DEKLARASI GLOBAL (Agar tidak error merah di method lain) ---
    private DefaultTableModel modelDashboard;
    private JTable tableDashboard;
    private CardLayout cardLayout;
    private JPanel mainContentCard;
    private ManajemenPrestasiDAO dao = new ManajemenPrestasiDAO();
    private Grafik halamanGrafik = new Grafik(); 
    private JButton btnDashboard, btnPrestasi, btnGrafik;
    private JLabel lblTotalPrestasi, lblTotalSiswa, lblTotalMenunggu, lblTotalDitolak;
    private ManajemenDataPrestasi panelManajemen = new ManajemenDataPrestasi();

    public DashboardAdmin() {
        setTitle("SISMONPRAS - Dashboard Admin");
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
        btnPrestasi = createSidebarButton("Manajemen Prestasi", new ListIcon(), false);
        btnGrafik = createSidebarButton("Grafik", new ChartIcon(), false);
        JButton btnLogout = createSidebarButton("Logout", null, false);

        sidebar.add(btnDashboard);
        sidebar.add(btnPrestasi);
        sidebar.add(btnGrafik);
        sidebar.add(btnLogout);

        // --- TOPBAR ---
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setPreferredSize(new Dimension(1200, 70));
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel titleLbl = new JLabel("ADMIN");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(0, 25, 0, 0));
        Color darkBlueSismon = new Color(23, 70, 122);
        titleLbl.setForeground(darkBlueSismon);
        topbar.add(titleLbl, BorderLayout.WEST);

        // --- MAIN CONTENT WITH CARDLAYOUT ---
        cardLayout = new CardLayout();
        mainContentCard = new JPanel(cardLayout);

        // Tambahkan Halaman Dashboard (Home)
        mainContentCard.add(createDashboardHome(), "HOME");
        mainContentCard.add(panelManajemen, "MANAJEMEN");
        mainContentCard.add(halamanGrafik, "GRAFIK");

        add(sidebar, BorderLayout.WEST);
        add(topbar, BorderLayout.NORTH);
        add(mainContentCard, BorderLayout.CENTER);

        // --- LOGIKA NAVIGASI ---
        btnDashboard.addActionListener(e -> {
            loadDataDashboard();
            updateNav(btnDashboard);
            cardLayout.show(mainContentCard, "HOME");
        });
        btnPrestasi.addActionListener(e -> {
            panelManajemen.loadTable();
            updateNav(btnPrestasi);
            cardLayout.show(mainContentCard, "MANAJEMEN");
        });
        btnGrafik.addActionListener(e -> {
        halamanGrafik.refreshGrafik(); // Update data dari database sebelum tampil
        updateNav(btnGrafik);
        cardLayout.show(mainContentCard, "GRAFIK");
        });
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin Logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });

        loadDataDashboard(); // Load data saat pertama kali buka
        setVisible(true);
    }
    // ... (Bagian import tetap sama)

public void loadDataDashboard() {
    mainContentCard.add(createDashboardHome(), "HOME");

    if (modelDashboard != null) {
        modelDashboard.setRowCount(0);
        try (ResultSet rs = dao.getData()) {
            while (rs != null && rs.next()) {
                modelDashboard.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("nama_siswa"),
                    rs.getString("nama_prestasi"),
                    rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    mainContentCard.revalidate();
    mainContentCard.repaint();
}
private JPanel createDashboardHome() {
        JPanel contentArea = new JPanel(new BorderLayout(0, 25));
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Statistik
        int totalP = dao.getCount("prestasi_akademik", null) + dao.getCount("prestasi_non", null);
        int totalS = dao.getCount("siswa", null);
        int pending = dao.getCount("prestasi_akademik", "status='Pending'") + dao.getCount("prestasi_non", "status='Pending'");
        int ditolak = dao.getCount("prestasi_akademik", "status='Ditolak'") + dao.getCount("prestasi_non", "status='Ditolak'");

        JPanel cardWrapper = new JPanel(new GridLayout(1, 4, 20, 0));
        cardWrapper.setOpaque(false);
        cardWrapper.add(createColoredCard("Total Prestasi", String.valueOf(totalP), new Color(41, 121, 255), new ChartIcon()));
        cardWrapper.add(createColoredCard("Total Siswa", String.valueOf(totalS), new Color(38, 170, 189), new ListIcon()));
        cardWrapper.add(createColoredCard("Menunggu", String.valueOf(pending), new Color(124, 179, 44), new UploadIcon()));
        cardWrapper.add(createColoredCard("Ditolak", String.valueOf(ditolak), new Color(233, 63, 63), new ListIcon()));

        // Header Tabel
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Daftar Riwayat Prestasi Terkini");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        Color darkBlueSismon = new Color(23, 70, 122);
        tableTitle.setForeground(darkBlueSismon);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(250, 30));
        searchPanel.add(new JLabel("Cari: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(searchPanel, BorderLayout.EAST);

        // Tabel
        String[] columns = {"ID", "Nama Prestasi", "Status"};
        modelDashboard = new DefaultTableModel(columns, 0);
        tableDashboard = new JTable(modelDashboard);
        tableDashboard.setRowHeight(40);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelDashboard);
        tableDashboard.setRowSorter(sorter);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText()));
            }
        });

        tableDashboard.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = (value != null) ? value.toString() : "";
                if(s.equals("Valid")) c.setForeground(new Color(46, 125, 50));
                else if(s.equals("Ditolak")) c.setForeground(Color.RED);
                else c.setForeground(Color.ORANGE);
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JPanel mainTablePanel = new JPanel(new BorderLayout(0, 10));
        mainTablePanel.setOpaque(false);
        mainTablePanel.add(tableHeader, BorderLayout.NORTH);
        mainTablePanel.add(new JScrollPane(tableDashboard), BorderLayout.CENTER);

        contentArea.add(cardWrapper, BorderLayout.NORTH);
        contentArea.add(mainTablePanel, BorderLayout.CENTER);

        return contentArea;
    }

    // --- BUTTON STYLING ---
    private void updateNav(JButton active) {
        JButton[] btns = {btnDashboard, btnPrestasi, btnGrafik};
        for(JButton b : btns) b.setBackground(new Color(25, 118, 210));
        active.setBackground(new Color(21, 101, 192));
    }

    private JButton createSidebarButton(String text, Icon icon, boolean isActive) {
        JButton btn = new JButton(text, icon);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(isActive ? new Color(21, 101, 192) : new Color(25, 118, 210));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        return btn;
    }
    
    class HomeIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

    class ChartIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(c.getForeground());
            g2.fillRect(x+2, y+10, 4, 7); g2.fillRect(x+8, y+6, 4, 11);
            g2.fillRect(x+14, y+3, 4, 14);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    private JPanel createColoredCard(String title, String value, Color bgColor, Icon watermarkIcon) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.translate(getWidth() - 65, getHeight() - 65);
                g2.scale(2.8, 2.8);
                if (watermarkIcon instanceof ChartIcon) {
                    g2.fillRect(2, 10, 4, 7); g2.fillRect(8, 6, 4, 11); g2.fillRect(14, 3, 4, 14);
                } else if (watermarkIcon instanceof ListIcon) {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRect(2, 3, 5, 5); g2.drawRect(2, 11, 5, 5);
                    g2.fillRect(10, 5, 8, 2); g2.fillRect(10, 13, 8, 2);
                } else if (watermarkIcon instanceof UploadIcon) {
                    g2.fillRect(9, 6, 2, 8);
                    g2.fillPolygon(new int[]{5, 10, 15}, new int[]{9, 4, 9}, 3);
                    g2.fillRect(3, 15, 14, 2);
                }
                g2.dispose();
            }
        };
        card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(255, 255, 255, 200));
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
}


