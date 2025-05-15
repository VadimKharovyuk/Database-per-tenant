package com.example.databasepertenant.dto;


import lombok.Data;

@Data
public class BookingDetailDTO {
    private BookingDTO booking;
    private FlightDTO flight;
    private UserResponseDTO user;
}
