package com.example.databasepertenant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightDTO {
    private Long id;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private Integer availableSeats;
    private String aircraft;

    // Дополнительное поле для хранения ID компании
    private String companyId;
    private String companyName; // Название авиакомпании
}