package model;
// MODUL 1 & 2: CLASS & OBJEK
public class Prestasi {
    // MODUL 4: ENKAPSULASI
    private String id;
    private String nama;
    private String idSiswa; 
    //Overloading (Constructor)
    public Prestasi(String id, String nama, String idSiswa) {
        this.id = id;
        this.nama = nama;
        this.idSiswa = idSiswa;
    }
    // Getter (Enkapsulasi)
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