package com.example.databasepertenant.repository;



import com.example.databasepertenant.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByFlightId(Long flightId);

    List<Booking> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Booking> findByPassengerEmail(String passengerEmail);
}
