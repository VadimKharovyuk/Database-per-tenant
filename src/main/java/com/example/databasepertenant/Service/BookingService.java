package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.CreateBookingDTO;
import com.example.databasepertenant.dto.UpdateBookingDTO;

import com.example.databasepertenant.maper.BookingMapper;
import com.example.databasepertenant.model.Booking;
import com.example.databasepertenant.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final FlightService flightService;

    /**
     * Получить все бронирования
     */
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти бронирование по ID
     */
    public Optional<BookingDTO> getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toDto);
    }

    /**
     * Создать новое бронирование
     */
    @Transactional
    public BookingDTO createBooking(CreateBookingDTO createBookingDTO) {
        // Проверяем доступность мест
        if (!flightService.decreaseAvailableSeats(createBookingDTO.getFlightId(), 1)) {
            throw new RuntimeException("Нет доступных мест для бронирования");
        }

        Booking booking = bookingMapper.toEntity(createBookingDTO);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Обновить бронирование
     */
    public Optional<BookingDTO> updateBooking(Long id, UpdateBookingDTO updateBookingDTO) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    bookingMapper.updateBookingFromDto(updateBookingDTO, booking);
                    Booking updatedBooking = bookingRepository.save(booking);
                    return bookingMapper.toDto(updatedBooking);
                });
    }

    /**
     * Отменить бронирование
     */
    @Transactional
    public boolean cancelBooking(Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    // Увеличиваем количество доступных мест
                    flightService.increaseAvailableSeats(booking.getFlight().getId(), 1);

                    // Удаляем бронирование
                    bookingRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Найти бронирования пользователя
     */
    public List<BookingDTO> findBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти бронирования для рейса
     */
    public List<BookingDTO> findBookingsByFlightId(Long flightId) {
        return bookingRepository.findByFlightId(flightId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Найти бронирования в определенный период времени
     */
    public List<BookingDTO> findBookingsByDateRange(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByBookingTimeBetween(start, end)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти бронирования по email пассажира
     */
    public List<BookingDTO> findBookingsByPassengerEmail(String passengerEmail) {
        return bookingRepository.findByPassengerEmail(passengerEmail)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}