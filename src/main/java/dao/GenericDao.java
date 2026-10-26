package dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T insert(T entity);
    boolean update(T entity);
    boolean delete(ID id);
    List<T> search(String query);
}
