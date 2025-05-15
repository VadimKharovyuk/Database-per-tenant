package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.BookingDTO;
import com.example.databasepertenant.dto.BookingDetailDTO;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.maper.BookingMapper;
import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.model.Booking;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.BookingRepository;
import com.example.databasepertenant.repository.FlightRepository;
import com.example.databasepertenant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final Map<String, BookingRepository> bookingRepositories;
    private final FlightServiceData flightServiceData;
    private final UserService userService;
    private final BookingMapper bookingMapper;


    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        // Получаем пользователя в общей базе
        User user = userService.getUserById(bookingDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем рейс и его тенант
        FlightDTO flightDTO = flightServiceData.getFlightById(bookingDTO.getFlightId())
                .orElseThrow(() -> new RuntimeException("Рейс не найден"));

        // Получаем ID тенанта
        String tenantId = bookingDTO.getCompanyId(); // Используем companyId из DTO

        // Получаем репозиторий для тенанта рейса
        BookingRepository tenantBookingRepository = bookingRepositories.get(tenantId);
        if (tenantBookingRepository == null) {
            throw new RuntimeException("Репозиторий для тенанта не найден: " + tenantId);
        }

        // Устанавливаем контекст тенанта
        TenantContext.setCurrentTenant(tenantId);

        try {
            // Проверяем наличие свободных мест
            if (flightDTO.getAvailableSeats() <= 0) {
                throw new RuntimeException("Нет свободных мест");
            }

            // Получаем сущность рейса
            Flight flight = flightServiceData.getFlightEntity(bookingDTO.getFlightId(), tenantId);

            // Создаем бронирование
            Booking booking = bookingMapper.toEntity(bookingDTO, flight);
            booking.setUserId(user.getId()); // ID пользователя из общей базы
            booking.setTenantId(tenantId);

            // Сохраняем бронирование в базе данных тенанта
            Booking savedBooking = tenantBookingRepository.save(booking);

            // Преобразуем и возвращаем DTO
            BookingDTO resultDto = bookingMapper.toDto(savedBooking);
            resultDto.setCompanyId(tenantId);

            return resultDto;
        } catch (Exception e) {
            log.error("Ошибка при создании бронирования: ", e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }



    // Метод отмены бронирования


    // Метод получения бронирований пользователя
    public List<BookingDTO> getUserBookings(Long userId) {
        List<BookingDTO> userBookings = new ArrayList<>();

        // Перебираем все репозитории бронирований тенантов
        for (Map.Entry<String, BookingRepository> entry : bookingRepositories.entrySet()) {
            String tenantId = entry.getKey();
            BookingRepository tenantBookingRepository = entry.getValue();

            TenantContext.setCurrentTenant(tenantId);

            try {
                // Ищем бронирования пользователя в каждой базе тенанта
                List<Booking> tenantBookings = tenantBookingRepository.findByUserId(userId);

                userBookings.addAll(
                        tenantBookings.stream()
                                .map(bookingMapper::toDto)
                                .toList()
                );
            } catch (Exception e) {
                log.error("Ошибка при получении бронирований для тенанта {}: ", tenantId, e);
            } finally {
                TenantContext.clear();
            }
        }

        return userBookings;
    }
}