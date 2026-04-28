package dao;

import java.sql.*;
import javax.swing.JOptionPane;
import koneksi.Koneksi;
import model.Prestasi;

public class PrestasiDAO {

    public void insertWithSertifikat(Prestasi p, String idTingkat, String idJenis, String kategori, String pathFile) {
    String tabel = kategori.equalsIgnoreCase("Akademik") ? "prestasi_akademik" : "prestasi_non";
    String kolomId = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";

    try (Connection c = Koneksi.getConnection()) {
        c.setAutoCommit(false); // Memulai transaksi agar kedua insert sukses bersamaan

        // 1. Insert ke tabel prestasi
        String sqlPres = "INSERT INTO " + tabel + " (" + kolomId + ", id_siswa, nama_prestasi, id_tingkat, id_jenis, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        PreparedStatement psPres = c.prepareStatement(sqlPres);
        psPres.setString(1, p.getId());
        psPres.setString(2, p.getIdSiswa());
        psPres.setString(3, p.getNama());
        psPres.setString(4, idTingkat);
        psPres.setString(5, idJenis);
        psPres.executeUpdate();

        // 2. Insert ke tabel sertifikat jika ada file yang diunggah
        if (pathFile != null && !pathFile.isEmpty()) {
            String sqlSertif = "INSERT INTO sertifikat (id_sertifikat, " + kolomId + ", nama_file, tanggal_terbit) VALUES (?, ?, ?, NOW())";
            PreparedStatement psSertif = c.prepareStatement(sqlSertif);
            psSertif.setString(1, "CERT-" + System.currentTimeMillis()); // Generate ID Sertifikat unik
            psSertif.setString(2, p.getId());
            psSertif.setString(3, pathFile);
            psSertif.executeUpdate();
        }

        c.commit(); // Simpan permanen
        JOptionPane.showMessageDialog(null, "Data dan Sertifikat Berhasil Disimpan!");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    // Cari method update di PrestasiDAO.java dan ubah menjadi seperti ini:
    public void update(Prestasi p, String idTingkat, String idJenis, String kategori, String path) {
    String tabel = kategori.equalsIgnoreCase("Akademik") ? "prestasi_akademik" : "prestasi_non";
    String kolomId = kategori.equalsIgnoreCase("Akademik") ? "id_prestasi_akademik" : "id_prestasi_non";

    // QUERY BERSIH: Tanpa file_path
    String sql = "UPDATE " + tabel + " SET nama_prestasi=?, id_tingkat=?, id_jenis=? WHERE " + kolomId + "=?";
    
    try (Connection c = Koneksi.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        
        ps.setString(1, p.getNama());
        ps.setString(2, idTingkat);
        ps.setString(3, idJenis);
        ps.setString(4, p.getId());
        
        ps.executeUpdate();
        JOptionPane.showMessageDialog(null, "Data Berhasil Diperbarui!");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error Update: " + e.getMessage());
        e.printStackTrace();
    }
    }

    // --- FITUR HAPUS ---
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
   

    // --- AMBIL DATA GABUNGAN (UNION) ---
   public ResultSet getPrestasiBySiswa(String idSiswa) throws Exception {
    Connection c = Koneksi.getConnection();

    // Query menggunakan LEFT JOIN untuk menghubungkan prestasi dengan sertifikatnya
    String sql = "SELECT p.id_prestasi_akademik AS id, p.nama_prestasi, p.status, 'Akademik' AS kategori, s.nama_file AS file_path " +
                 "FROM prestasi_akademik p " +
                 "LEFT JOIN sertifikat s ON p.id_prestasi_akademik = s.id_prestasi_akademik " +
                 "WHERE p.id_siswa = ? " +
                 "UNION " +
                 "SELECT n.id_prestasi_non AS id, n.nama_prestasi, n.status, 'Non-Akademik' AS kategori, s.nama_file AS file_path " +
                 "FROM prestasi_non n " +
                 "LEFT JOIN sertifikat s ON n.id_prestasi_non = s.id_prestasi_non " +
                 "WHERE n.id_siswa = ?";

    PreparedStatement ps = c.prepareStatement(sql);
    ps.setString(1, idSiswa);
    ps.setString(2, idSiswa);

    return ps.executeQuery();
    }
    
    public ResultSet search(String key) throws Exception {
        Connection c = Koneksi.getConnection();
        String sql = "SELECT * FROM (" +
                     "  SELECT id_prestasi_akademik AS id, nama_prestasi, status, 'Akademik' AS kategori, file_path FROM prestasi_akademik " +
                     "  UNION " +
                     "  SELECT id_prestasi_non AS id, nama_prestasi, status, 'Non-Akademik' AS kategori, file_path FROM prestasi_non" +
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