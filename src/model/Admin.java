package model;
class Admin extends User {
    
    @Override
    public void dashboard() {
        System.out.println("Dashboard Admin");
    }
}