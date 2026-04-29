package dao;

import model.Prestasi;
// MODUL 6: INTERFACE
public interface CRUD {

    void insert(Prestasi p);
    void update(Prestasi p);
    void delete(String id);
}