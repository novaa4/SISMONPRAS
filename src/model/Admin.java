package model;
// MODUL 3: INHERITANCE
class Admin extends User {
    // MODUL 5: POLYMORPHISM (Overriding)
    @Override
    public void dashboard() {
        System.out.println("Dashboard Admin");
    }
}