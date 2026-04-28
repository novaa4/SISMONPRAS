package model;

public class Prestasi {
    private String id;
    private String nama;
    private String idSiswa; 

    public Prestasi(String id, String nama, String idSiswa) {
        this.id = id;
        this.nama = nama;
        this.idSiswa = idSiswa;
    }
    public String getIdSiswa() {
        return idSiswa;
    }
    public String getId() {
        return id;
    }
    public String getNama() {
        return nama;
    }
}