package com.example.databasepertenant.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long userId; // ID пользователя из общей базы
    private Long flightId;
    private String companyId; // ID компании (тенанта)
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private BigDecimal paidAmount;
    private LocalDateTime bookingTime;

}