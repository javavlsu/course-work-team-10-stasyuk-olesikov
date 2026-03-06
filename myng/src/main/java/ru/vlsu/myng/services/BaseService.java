package ru.vlsu.myng.services;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// т.к. сервисы часто повторяют стандартные операции jpa repository
// можно добавить наследование от этого сервиса чтобы избавиться от a ton of boilerplate bullshit
public abstract class BaseService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    protected BaseService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found"));
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public void delete(ID id) {
        repository.deleteById(id);
    }
}