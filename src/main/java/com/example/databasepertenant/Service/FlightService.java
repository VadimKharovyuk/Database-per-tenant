package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.CreateFlightDTO;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.dto.UpdateFlightDTO;

import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.repository.FlightRepository;
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
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    /**
     * Получить все рейсы
     */
    public List<FlightDTO> getAllFlights() {
        return flightRepository.findAll()
                .stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти рейс по ID
     */
    public Optional<FlightDTO> getFlightById(Long id) {
        return flightRepository.findById(id)
                .map(flightMapper::toDto);
    }

    /**
     * Создать новый рейс
     */
    public FlightDTO createFlight(CreateFlightDTO createFlightDTO) {
        Flight flight = flightMapper.toEntity(createFlightDTO);
        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.toDto(savedFlight);
    }

    /**
     * Обновить рейс
     */
    public Optional<FlightDTO> updateFlight(Long id, UpdateFlightDTO updateFlightDTO) {
        return flightRepository.findById(id)
                .map(flight -> {
                    flightMapper.updateFlightFromDto(updateFlightDTO, flight);
                    Flight updatedFlight = flightRepository.save(flight);
                    return flightMapper.toDto(updatedFlight);
                });
    }

    /**
     * Удалить рейс
     */
    public boolean deleteFlight(Long id) {
        if (flightRepository.existsById(id)) {
            flightRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Найти рейсы по аэропортам отправления и прибытия
     */
    public List<FlightDTO> findFlightsByAirports(String departureAirport, String arrivalAirport) {
        return flightRepository.findByDepartureAirportAndArrivalAirport(departureAirport, arrivalAirport)
                .stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти рейсы в определенный период времени
     */
    public List<FlightDTO> findFlightsByDateRange(LocalDateTime start, LocalDateTime end) {
        return flightRepository.findByDepartureTimeBetween(start, end)
                .stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти рейсы по аэропортам и периоду времени
     */
    public List<FlightDTO> findFlightsByAirportsAndDateRange(
            String departureAirport,
            String arrivalAirport,
            LocalDateTime start,
            LocalDateTime end) {
        return flightRepository.findByDepartureAirportAndArrivalAirportAndDepartureTimeBetween(
                        departureAirport, arrivalAirport, start, end)
                .stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти рейсы по номеру
     */
    public List<FlightDTO> findFlightsByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Уменьшить количество доступных мест
     */
    public boolean decreaseAvailableSeats(Long flightId, int count) {
        return flightRepository.findById(flightId)
                .map(flight -> {
                    if (flight.getAvailableSeats() >= count) {
                        flight.setAvailableSeats(flight.getAvailableSeats() - count);
                        flightRepository.save(flight);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Увеличить количество доступных мест (при отмене бронирования)
     */
    public boolean increaseAvailableSeats(Long flightId, int count) {
        return flightRepository.findById(flightId)
                .map(flight -> {
                    flight.setAvailableSeats(flight.getAvailableSeats() + count);
                    flightRepository.save(flight);
                    return true;
                })
                .orElse(false);
    }
}