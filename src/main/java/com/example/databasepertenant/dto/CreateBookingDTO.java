package com.example.databasepertenant.dto;

import lombok.AllArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDTO {
    private Long userId;
    private Long flightId;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private BigDecimal paidAmount;
}
