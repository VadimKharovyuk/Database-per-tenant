package com.example.databasepertenant.Service;

import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.repository.FlightRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class FlightRepositoryImpl implements FlightRepository {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAll() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery("SELECT f FROM Flight f", Flight.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Flight> findById(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(Flight.class, id));
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional
    public Flight save(Flight flight) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (flight.getId() == null) {
                entityManager.persist(flight);
            } else {
                flight = entityManager.merge(flight);
            }
            entityManager.getTransaction().commit();
            return flight;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Flight flight = entityManager.find(Flight.class, id);
            if (flight != null) {
                entityManager.remove(flight);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}