package com.example.databasepertenant.maper;


import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.CreateBookingDTO;
import com.example.databasepertenant.dto.UpdateBookingDTO;
import com.example.databasepertenant.model.Booking;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.FlightRepository;
import com.example.databasepertenant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingMapper {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;

    public BookingMapper(UserRepository userRepository, @Qualifier("company1Repository") FlightRepository flightRepository) {
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Преобразует объект Booking в BookingDTO
     */
    public BookingDTO toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUsername(booking.getUser().getUsername());
        }

        if (booking.getFlight() != null) {
            dto.setFlightId(booking.getFlight().getId());
            dto.setFlightNumber(booking.getFlight().getFlightNumber());
            dto.setDepartureAirport(booking.getFlight().getDepartureAirport());
            dto.setArrivalAirport(booking.getFlight().getArrivalAirport());
            dto.setDepartureTime(booking.getFlight().getDepartureTime());
            dto.setArrivalTime(booking.getFlight().getArrivalTime());
        }

        dto.setPassengerName(booking.getPassengerName());
        dto.setPassengerEmail(booking.getPassengerEmail());
        dto.setSeatNumber(booking.getSeatNumber());
        dto.setPaidAmount(booking.getPaidAmount());
        dto.setBookingTime(booking.getBookingTime());

        return dto;
    }

    /**
     * Преобразует CreateBookingDTO в Booking
     */
    public Booking toEntity(CreateBookingDTO dto) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();

        // Находим пользователя по ID
        User user = userRepository.findById(dto.getUserId()).orElse(null);
        booking.setUser(user);

        // Находим рейс по ID
        Flight flight = flightRepository.findById(dto.getFlightId()).orElse(null);
        booking.setFlight(flight);

        booking.setPassengerName(dto.getPassengerName());
        booking.setPassengerEmail(dto.getPassengerEmail());
        booking.setSeatNumber(dto.getSeatNumber());
        booking.setPaidAmount(dto.getPaidAmount());
        booking.setBookingTime(LocalDateTime.now());

        return booking;
    }

    /**
     * Обновляет Booking на основе UpdateBookingDTO
     */
    public void updateBookingFromDto(UpdateBookingDTO dto, Booking booking) {
        if (dto == null || booking == null) {
            return;
        }

        booking.setPassengerName(dto.getPassengerName());
        booking.setPassengerEmail(dto.getPassengerEmail());
        booking.setSeatNumber(dto.getSeatNumber());
    }
}
