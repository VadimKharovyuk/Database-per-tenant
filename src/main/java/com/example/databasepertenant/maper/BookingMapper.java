package com.example.databasepertenant.maper;

import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.BookingDetailDTO;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.dto.UserResponseDTO;
import com.example.databasepertenant.model.Booking;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.model.User;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public Booking toEntity(BookingDTO dto, Flight flight) {
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setUserId(dto.getUserId());
        booking.setFlight(flight);
        booking.setTenantId(dto.getCompanyId());
        booking.setPassengerName(dto.getPassengerName());
        booking.setPassengerEmail(dto.getPassengerEmail());
        booking.setSeatNumber(dto.getSeatNumber());
        booking.setPaidAmount(dto.getPaidAmount());

        return booking;
    }

    public BookingDTO toDto(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setFlightId(booking.getFlight().getId());
        dto.setCompanyId(booking.getTenantId());
        dto.setPassengerName(booking.getPassengerName());
        dto.setPassengerEmail(booking.getPassengerEmail());
        dto.setSeatNumber(booking.getSeatNumber());
        dto.setPaidAmount(booking.getPaidAmount());
        dto.setBookingTime(booking.getBookingTime());

        return dto;
    }


}