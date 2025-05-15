package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.BookingService;
import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.BookingDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        BookingDTO createdBooking = bookingService.createBooking(bookingDTO);
        return ResponseEntity.ok(createdBooking);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getUserBookings(@PathVariable Long userId) {
        List<BookingDTO> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }



}