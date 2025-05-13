package com.example.databasepertenant.Controller;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.Service.BookingService;
import com.example.databasepertenant.Service.FlightService;
import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.CreateBookingDTO;
import com.example.databasepertenant.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final BookingService bookingService;

    @GetMapping("/flights")
    public String showFlights(
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader,
            Model model) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setTenantId(tenantId);
            try {
                List<FlightDTO> flights = flightService.getAllFlights();
                model.addAttribute("flights", flights);
                model.addAttribute("tenantId", tenantId);
            } finally {
                TenantContext.clear();
            }
        } else {
            return "redirect:/?error=noTenant";
        }

        return "flights";
    }

    @GetMapping("/flights/search")
    public String searchFlights(
            @RequestParam(value = "departureAirport", required = false) String departureAirport,
            @RequestParam(value = "arrivalAirport", required = false) String arrivalAirport,
            @RequestParam(value = "departureDate", required = false) String departureDateStr,
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader,
            Model model) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            List<FlightDTO> flights;

            if (departureAirport != null && !departureAirport.isEmpty()
                    && arrivalAirport != null && !arrivalAirport.isEmpty()) {

                if (departureDateStr != null && !departureDateStr.isEmpty()) {
                    LocalDateTime departureDate = LocalDateTime.parse(departureDateStr + "T00:00:00");
                    LocalDateTime nextDay = departureDate.plusDays(1);

                    flights = flightService.findFlightsByAirportsAndDateRange(
                            departureAirport, arrivalAirport, departureDate, nextDay);
                } else {
                    flights = flightService.findFlightsByAirports(departureAirport, arrivalAirport);
                }
            } else {
                flights = flightService.getAllFlights();
            }

            model.addAttribute("flights", flights);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("departureAirport", departureAirport);
            model.addAttribute("arrivalAirport", arrivalAirport);
            model.addAttribute("departureDate", departureDateStr);

        } finally {
            TenantContext.clear();
        }

        return "flights";
    }

    @GetMapping("/flights/{id}")
    public String flightDetails(
            @PathVariable Long id,
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader,
            Model model) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            FlightDTO flight = flightService.getFlightById(id)
                    .orElseThrow(() -> new RuntimeException("Рейс не найден"));

            model.addAttribute("flight", flight);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("booking", new CreateBookingDTO());

        } finally {
            TenantContext.clear();
        }

        return "flight-details";
    }

    @PostMapping("/bookings")
    public String createBooking(
            @ModelAttribute CreateBookingDTO createBookingDTO,
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            BookingDTO booking = bookingService.createBooking(createBookingDTO);
            return "redirect:/bookings/" + booking.getId() + "?tenantId=" + tenantId;
        } catch (Exception e) {
            return "redirect:/flights/" + createBookingDTO.getFlightId() + "?tenantId=" + tenantId + "&error=bookingFailed";
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/bookings")
    public String userBookings(
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader,
            @RequestParam(value = "userId", required = true) Long userId,
            Model model) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            List<BookingDTO> bookings = bookingService.findBookingsByUserId(userId);
            model.addAttribute("bookings", bookings);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("userId", userId);
        } finally {
            TenantContext.clear();
        }

        return "bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(
            @PathVariable Long id,
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader,
            Model model) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            BookingDTO booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

            model.addAttribute("booking", booking);
            model.addAttribute("tenantId", tenantId);
        } finally {
            TenantContext.clear();
        }

        return "booking-details";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            @RequestParam(value = "tenantId", required = false) String tenantIdParam,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader) {

        String tenantId = tenantIdParam != null ? tenantIdParam : tenantIdHeader;

        if (tenantId == null || tenantId.isEmpty()) {
            return "redirect:/?error=noTenant";
        }

        TenantContext.setTenantId(tenantId);
        try {
            BookingDTO booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

            bookingService.cancelBooking(id);

            return "redirect:/bookings?tenantId=" + tenantId + "&userId=" + booking.getUserId() + "&cancelled=true";
        } catch (Exception e) {
            return "redirect:/bookings/" + id + "?tenantId=" + tenantId + "&error=cancelFailed";
        } finally {
            TenantContext.clear();
        }
    }
}