package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.*;
import koneksi.Koneksi;

public class LoginForm extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JCheckBox showPassword;

    private Color blueColor = new Color(52, 152, 219);
    private Color grayColor = new Color(127, 140, 141);
    private Color darkBlue = new Color(44, 62, 80);

    public LoginForm() {
        setTitle("SISMONPRAS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setUndecorated(false); 

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
       
                java.net.URL imgURL = getClass().getResource("/view/img/background.png.png");
                if (imgURL != null) {
                    ImageIcon img = new ImageIcon(imgURL);
                    g.drawImage(img.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
              
                    g.setColor(new Color(236, 240, 241));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
     
        mainPanel.setLayout(new GridBagLayout()); 
        setContentPane(mainPanel);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(380, 480));
        card.setBackground(new Color(255, 255, 255, 245)); 
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        JLabel titleLabel = new JLabel("SISMONPRAS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(darkBlue);
        titleLabel.setBounds(0, 40, 380, 40);
        card.add(titleLabel);

        userField = new JTextField("Username");
        userField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        userField.setForeground(grayColor);
        userField.setBounds(40, 130, 300, 40);
        userField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, grayColor));
        addPlaceholder(userField, "Username");
        card.add(userField);

        passField = new JPasswordField();
        passField.setEchoChar((char) 0); 
        passField.setText("Password");
        passField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passField.setForeground(grayColor);
        passField.setBounds(40, 200, 300, 40);
        passField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, grayColor));
        addPlaceholder(passField, "Password");
        card.add(passField);

        showPassword = new JCheckBox("Lihat Password");
        showPassword.setOpaque(false); // Agar transparan mengikuti warna card
        showPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPassword.setForeground(grayColor);
        showPassword.setBounds(40, 245, 150, 20);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                if (!new String(passField.getPassword()).equals("Password")) {
                    passField.setEchoChar('\u25CF');
                }
            }
        });
        card.add(showPassword);

        JButton loginButton = new JButton("MASUK");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(blueColor);
        loginButton.setBounds(40, 310, 300, 45);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(blueColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(blueColor);
            }
        });
        loginButton.addActionListener(e -> performLogin());
        card.add(loginButton);

        JLabel footerLabel = new JLabel("Masuk ke sistem Admin / Siswa", SwingConstants.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footerLabel.setForeground(grayColor);
        footerLabel.setBounds(0, 390, 380, 25);
        card.add(footerLabel);

        mainPanel.add(card);

        setVisible(true);
    }

    private void addPlaceholder(JTextField field, String hint) {
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(hint)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    if (field instanceof JPasswordField && !showPassword.isSelected()) {
                        ((JPasswordField) field).setEchoChar('\u25CF');
                    }
                }
                field.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, blueColor));
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(grayColor);
                    field.setText(hint);
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                }
                field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, grayColor));
            }
        });
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.equals("Username") || password.equals("Password") || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password wajib diisi!");
            return;
        }

        try {
            Connection conn = Koneksi.getConnection();
            String sql = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("id_role");
                String idSiswa = rs.getString("id_siswa");
                JOptionPane.showMessageDialog(this, "Login Berhasil!");

                if ("R02".equalsIgnoreCase(role)) {
                    new DashboardAdmin().setVisible(true);
                } else {
        
                    new DashboardSiswaModern(idSiswa).setVisible(true); 
                }
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password salah!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Kesalahan Sistem: " + e.getMessage());
        }
    }
}