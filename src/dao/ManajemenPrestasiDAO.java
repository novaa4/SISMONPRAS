package dao;

import java.sql.*;
import javax.swing.JOptionPane;
import koneksi.Koneksi;
import model.Prestasi;

public class ManajemenPrestasiDAO {

    public boolean updateStatus(String id, String statusBaru, String kategori) {
        String tabel = kategori.equalsIgnoreCase("Akademik") ? "prestasi_akademik" : "prestasi_non";
        String kolomId = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";
        
        String sql = "UPDATE " + tabel + " SET status = ? WHERE " + kolomId + " = ?";
        
        try (Connection c = Koneksi.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusBaru);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void insert(Prestasi p, String idTingkat, String idJenis, String kategori) {
        String tabel = kategori.equalsIgnoreCase("Akademik") ? "prestasi_akademik" : "prestasi_non";
        String kolomId = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";

        String sql = "INSERT INTO " + tabel + " (" + kolomId + ", id_siswa, nama_prestasi, id_tingkat, id_jenis, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'Pending')";
        
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, p.getId());
            ps.setString(2, p.getIdSiswa()); // Dinamis: Mengambil ID siswa yang login
            ps.setString(3, p.getNama());
            ps.setString(4, idTingkat);
            ps.setString(5, idJenis);
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data " + kategori + " Berhasil Disimpan!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal Simpan: " + e.getMessage());
        }
    }

    public void update(Prestasi p, String idTingkat, String idJenis, String kategori) {
        String tabel = kategori.equalsIgnoreCase("Akademik") ? "prestasi_akademik" : "prestasi_non";
        String kolomId = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";

        String sql = "UPDATE " + tabel + " SET nama_prestasi=?, id_tingkat=?, id_jenis=? WHERE " + kolomId + "=?";
        
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, idTingkat);
            ps.setString(3, idJenis);
            ps.setString(4, p.getId());
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data Berhasil Diperbarui!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal Update: " + e.getMessage());
        }
    }

    public void delete(String id, String kategori) {
    // 1. Bersihkan variabel dari spasi liar dan pastikan tidak null
    String idBersih = (id != null) ? id.trim() : "";
    String katBersih = (kategori != null) ? kategori.trim() : "";

    // 2. Logika penentuan tabel yang lebih fleksibel
    String tabel, kolomId;
    
    // Gunakan .contains atau .equalsIgnoreCase yang mencakup kemungkinan teks dari JTable
    if (katBersih.equalsIgnoreCase("Akademik") || katBersih.contains("Akademik")) {
        tabel = "prestasi_akademik";
        kolomId = "id_prestasi_akademik";
    } else {
        tabel = "prestasi_non";
        kolomId = "id_prestasi_non";
    }

    // 3. Eksekusi Query
    String sql = "DELETE FROM " + tabel + " WHERE " + kolomId + " = ?";
    
    // DEBUG: Cek di console NetBeans apakah query-nya sudah benar
    System.out.println("DEBUG DELETE -> Tabel: " + tabel + " | Kolom: " + kolomId + " | ID: " + idBersih);

    try (Connection c = Koneksi.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        
        ps.setString(1, idBersih);
        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(null, "Data " + katBersih + " Berhasil Dihapus!");
        } else {
            // Jika masuk ke sini, berarti SQL jalan tapi ID tidak ketemu di tabel tersebut
            JOptionPane.showMessageDialog(null, "Gagal: Data dengan ID " + idBersih + " tidak ditemukan di tabel " + tabel);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
    }
}
    // --- AMBIL SEMUA DATA (UNTUK ADMIN) ---
    public ResultSet getData() throws Exception {
    Connection c = Koneksi.getConnection();
    // Query dengan JOIN untuk mengambil Nama Siswa dan Path Sertifikat
    String sql = "SELECT p.id_prestasi_akademik AS id, s.nama_siswa, p.nama_prestasi, p.status, 'Akademik' AS kategori, ser.nama_file AS path " +
                 "FROM prestasi_akademik p " +
                 "JOIN siswa s ON p.id_siswa = s.id_siswa " +
                 "LEFT JOIN sertifikat ser ON p.id_prestasi_akademik = ser.id_prestasi_akademik " +
                 "UNION " +
                 "SELECT p.id_prestasi_non AS id, s.nama_siswa, p.nama_prestasi, p.status, 'Non-Akademik' AS kategori, ser.nama_file AS path " +
                 "FROM prestasi_non p " +
                 "JOIN siswa s ON p.id_siswa = s.id_siswa " +
                 "LEFT JOIN sertifikat ser ON p.id_prestasi_non = ser.id_prestasi_non";
    return c.createStatement().executeQuery(sql);
    }
    // --- AMBIL DATA PER SISWA (UNTUK DASHBOARD SISWA/USER) ---
    public ResultSet getPrestasiBySiswa(String idSiswa) throws Exception {
    Connection c = Koneksi.getConnection();
    String sql = "SELECT p.id_prestasi_akademik AS id, p.nama_prestasi, p.status, 'Akademik' AS kategori, ser.nama_file AS path " +
                 "FROM prestasi_akademik p " +
                 "LEFT JOIN sertifikat ser ON p.id_prestasi_akademik = ser.id_prestasi_akademik " +
                 "WHERE p.id_siswa = ? " +
                 "UNION " +
                 "SELECT p.id_prestasi_non AS id, p.nama_prestasi, p.status, 'Non-Akademik' AS kategori, ser.nama_file AS path " +
                 "FROM prestasi_non p " +
                 "LEFT JOIN sertifikat ser ON p.id_prestasi_non = ser.id_prestasi_non " +
                 "WHERE p.id_siswa = ?";
    
    PreparedStatement ps = c.prepareStatement(sql);
    ps.setString(1, idSiswa);
    ps.setString(2, idSiswa);
    return ps.executeQuery();
    }
    
    public int getPendingCount() {
    int total = 0;
    String sql = "SELECT " +
                 "(SELECT COUNT(*) FROM prestasi_akademik WHERE status = 'Pending') + " +
                 "(SELECT COUNT(*) FROM prestasi_non WHERE status = 'Pending') AS total_pending";
    try (Connection c = Koneksi.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        if (rs.next()) total = rs.getInt("total_pending");
    } catch (Exception e) { e.printStackTrace(); }
    return total;
    }
    public int getCount(String table, String condition) {
        int total = 0;
        String sql = "SELECT COUNT(*) AS total FROM " + table;
        if (condition != null && !condition.isEmpty()) {
        sql += " WHERE " + condition;
    }
    
    try (Connection c = Koneksi.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        if (rs.next()) {
            total = rs.getInt("total");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return total;
    }
    public ResultSet search(String key) throws Exception {
    Connection c = Koneksi.getConnection();
    // Kita tambahkan kolom 'kategori' manual di dalam masing-masing SELECT
    String sql = "SELECT * FROM (" +
                 "  SELECT id_prestasi_akademik AS id, nama_prestasi, status, 'Akademik' AS kategori FROM prestasi_akademik " +
                 "  UNION " +
                 "  SELECT id_prestasi_non AS id, nama_prestasi, status, 'Non-Akademik' AS kategori FROM prestasi_non" +
                 ") AS gabungan WHERE nama_prestasi LIKE ? OR id LIKE ?";
                 
    PreparedStatement ps = c.prepareStatement(sql);
    ps.setString(1, "%" + key + "%");
    ps.setString(2, "%" + key + "%");
    return ps.executeQuery();
    }
    
    public ResultSet getTingkat() throws Exception {
        return Koneksi.getConnection().createStatement().executeQuery("SELECT * FROM tingkat");
    }

    public ResultSet getJenis() throws Exception {
        return Koneksi.getConnection().createStatement().executeQuery("SELECT * FROM jenis");
    }
}