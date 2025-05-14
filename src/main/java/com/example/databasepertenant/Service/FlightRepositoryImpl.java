package com.example.databasepertenant.Service;

import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.repository.FlightRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FlightRepositoryImpl implements FlightRepository {

    private final EntityManagerFactory entityManagerFactory;

    public FlightRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

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
    public List<Flight> findAll(Sort sort) {
        // Для простоты реализации мы не обрабатываем сортировку здесь
        return findAll();
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

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            String origin, String destination, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.origin = :origin AND " +
                            "f.destination = :destination AND " +
                            "f.departureTime BETWEEN :startDate AND :endDate",
                    Flight.class);
            query.setParameter("origin", origin);
            query.setParameter("destination", destination);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByOriginAndDestination(String origin, String destination) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.origin = :origin AND " +
                            "f.destination = :destination",
                    Flight.class);
            query.setParameter("origin", origin);
            query.setParameter("destination", destination);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByDepartureTimeBetween(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.departureTime BETWEEN :startDate AND :endDate",
                    Flight.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByAvailableSeatsGreaterThan(int minSeats) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.availableSeats > :minSeats",
                    Flight.class);
            query.setParameter("minSeats", minSeats);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Transactional(readOnly = true)
    public List<Flight> findByOriginAndDepartureTimeBetween(
            String origin, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.origin = :origin AND " +
                            "f.departureTime BETWEEN :startDate AND :endDate",
                    Flight.class);
            query.setParameter("origin", origin);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByDestinationAndDepartureTimeBetween(
            String destination, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.destination = :destination AND " +
                            "f.departureTime BETWEEN :startDate AND :endDate",
                    Flight.class);
            query.setParameter("destination", destination);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByFlightNumber(String flightNumber) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Flight> query = entityManager.createQuery(
                    "SELECT f FROM Flight f WHERE " +
                            "f.flightNumber = :flightNumber",
                    Flight.class);
            query.setParameter("flightNumber", flightNumber);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    // Реализация остальных методов JpaRepository (можно оставить заглушки или не реализовывать)

    @Override
    @Transactional
    public <S extends Flight> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new java.util.ArrayList<>();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (S entity : entities) {
                if (entity.getId() == null) {
                    entityManager.persist(entity);
                } else {
                    entity = entityManager.merge(entity);
                }
                result.add(entity);
            }
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(Flight.class, id) != null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAllById(Iterable<Long> ids) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT f FROM Flight f WHERE f.id IN (");
            boolean first = true;
            for (Long id : ids) {
                if (!first) {
                    queryBuilder.append(",");
                }
                queryBuilder.append(id);
                first = false;
            }
            queryBuilder.append(")");

            if (first) { // empty iterable
                return List.of();
            }

            TypedQuery<Flight> query = entityManager.createQuery(queryBuilder.toString(), Flight.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long count() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return entityManager.createQuery("SELECT COUNT(f) FROM Flight f", Long.class).getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional
    public void delete(Flight entity) {
        if (entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<? extends Long> ids) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (Long id : ids) {
                Flight flight = entityManager.find(Flight.class, id);
                if (flight != null) {
                    entityManager.remove(flight);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends Flight> entities) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (Flight entity : entities) {
                if (entity.getId() != null) {
                    Flight flight = entityManager.find(Flight.class, entity.getId());
                    if (flight != null) {
                        entityManager.remove(flight);
                    }
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Flight").executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    // Методы, которые не требуются для нашей функциональности, можно оставить с заглушками

    @Override
    public <S extends Flight> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> long count(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Page<Flight> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Flight> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteAllInBatch(Iterable<Flight> entities) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Flight getOne(Long id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Flight getById(Long id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Flight getReferenceById(Long id) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}