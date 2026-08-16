package za.ac.cput.service;

import java.util.List;

/** Generic CRUD contract every service implements,
  *  so callers deal with one consistent shape. */
public interface IService <T, ID>{
    T create(T entity);
    T read(ID id);
    T update(T entity);
    void delete(ID id);
    List<T> getAll();
}
