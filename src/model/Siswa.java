package model;
// MODUL 3: INHERITANCE
class Siswa extends User {
    // MODUL 5: POLYMORPHISM (Overriding)
    @Override
    public void dashboard() {
        System.out.println("Dashboard Siswa");
    }
}