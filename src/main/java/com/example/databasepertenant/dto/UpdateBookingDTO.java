package com.example.databasepertenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingDTO {
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
}
