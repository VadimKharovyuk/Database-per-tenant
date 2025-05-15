package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.BookingService;
import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.Service.UserService;
import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final FlightServiceData flightServiceData;
    private final UserService userService;

    // Страница создания бронирования
    @GetMapping("/create")
    public String showBookingForm(
            @RequestParam("flightId") Long flightId,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Получаем детали рейса
        FlightDTO flight = flightServiceData.getFlightById(flightId)
                .orElseThrow(() -> new RuntimeException("Рейс не найден"));

        // Создаем DTO для бронирования
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFlightId(flightId);
        bookingDTO.setCompanyId(flight.getCompanyId()); // Важно указать companyId

        model.addAttribute("booking", bookingDTO);
        model.addAttribute("flight", flight);
        model.addAttribute("username", userDetails.getUsername());

        return "bookings/create";
    }

    // Обработка создания бронирования
    @PostMapping("/create")
    public String createBooking(
            @ModelAttribute("booking") BookingDTO bookingDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        try {
            // Получаем ID пользователя
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            bookingDTO.setUserId(user.getId());

            // Создаем бронирование
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO);

            // Перенаправляем на страницу успеха
            return "redirect:/bookings/success?bookingId=" + createdBooking.getId() +
                    "&companyId=" + createdBooking.getCompanyId();
        } catch (Exception e) {
            // Обработка ошибок
            model.addAttribute("error", e.getMessage());
            return "bookings/create";
        }
    }

    // Страница списка бронирований
    @GetMapping
    public String getUserBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        // Получаем ID пользователя
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем список бронирований
        List<BookingDTO> bookings = bookingService.getUserBookings(user.getId());

        model.addAttribute("bookings", bookings);
        return "bookings/list";
    }

    // Страница успешного бронирования
    @GetMapping("/success")
    public String bookingSuccess(
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("companyId") String companyId,
            Model model
    ) {
        // Можно добавить дополнительную логику, например, получение деталей бронирования
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("companyId", companyId);
        return "bookings/success";
    }

}