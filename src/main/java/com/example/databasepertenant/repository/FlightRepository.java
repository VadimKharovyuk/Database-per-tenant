package com.example.databasepertenant.repository;

import com.example.databasepertenant.model.Flight;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {
    List<Flight> findAll();
    Optional<Flight> findById(Long id);
    Flight save(Flight flight);
    void deleteById(Long id);
}