package com.example.databasepertenant.repository;

import com.example.databasepertenant.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            String origin,
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Flight> findByOriginAndDestination(String origin, String destination);

    List<Flight> findByDepartureTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Flight> findByAvailableSeatsGreaterThan(int minSeats);

    List<Flight> findByOriginAndDepartureTimeBetween(
            String origin,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Flight> findByDestinationAndDepartureTimeBetween(
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Flight> findByFlightNumber(String flightNumber);
}