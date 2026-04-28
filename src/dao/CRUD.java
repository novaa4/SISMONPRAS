package dao;

import model.Prestasi;

public interface CRUD {

    void insert(Prestasi p);
    void update(Prestasi p);
    void delete(String id);
}