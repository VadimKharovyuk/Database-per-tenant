package com.example.databasepertenant.repository;

import com.example.databasepertenant.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepartureAirportAndArrivalAirport(String departureAirport, String arrivalAirport);

    List<Flight> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Flight> findByDepartureAirportAndArrivalAirportAndDepartureTimeBetween(
            String departureAirport,
            String arrivalAirport,
            LocalDateTime start,
            LocalDateTime end);

    List<Flight> findByFlightNumber(String flightNumber);
}