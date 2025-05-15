package com.example.databasepertenant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID пользователя из общей базы данных
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId; // ID тенанта (авиакомпании)

    @Column(nullable = false)
    private String passengerName;

    @Column(nullable = false)
    private String passengerEmail;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private LocalDateTime bookingTime;


}