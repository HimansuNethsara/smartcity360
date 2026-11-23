package lk.jiat.smartcity360.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractRepository<T, ID extends Serializable> {

    @PersistenceContext(unitName = "smartcity360PU")
    protected EntityManager em;

    protected abstract Class<T> entityClass();

    public T findById(ID id) {
        return em.find(entityClass(), id);
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass().getSimpleName() + " e", entityClass())
                .getResultList();
    }

    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    public T update(T entity) {
        return em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public void deleteById(ID id) {
        T ref = findById(id);
        if (ref != null) {
            delete(ref);
        }
    }

    public void flush() {
        em.flush();
    }

    public void refresh(T entity) {
        em.refresh(entity);
    }
}