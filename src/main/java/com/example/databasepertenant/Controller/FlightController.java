package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightServiceData flightServiceData;

    /**
     * Получить все рейсы из всех авиакомпаний
     */
    @GetMapping
    public ResponseEntity<?> getAllFlights() {
        try {
            List<FlightDTO> flights = flightServiceData.getAllFlights();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", flights.size(),
                    "flights", flights
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error retrieving flights: " + e.getMessage()
            ));
        }
    }

    /**
     * Получить рейсы конкретной авиакомпании
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getCompanyFlights(@PathVariable String companyId) {
        try {
            List<FlightDTO> flights = flightServiceData.getCompanyFlights(companyId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", flights.size(),
                    "company", companyId,
                    "flights", flights
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error retrieving flights for company " + companyId + ": " + e.getMessage()
            ));
        }
    }

    /**
     * Поиск рейсов по параметрам
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        try {
            // Преобразуем LocalDate в LocalDateTime (начало и конец дня)
            LocalDateTime startOfDay = departureDate.atStartOfDay();
            LocalDateTime endOfDay = departureDate.atTime(LocalTime.MAX);

            List<FlightDTO> flights = flightServiceData.searchFlights(origin, destination, startOfDay, endOfDay);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", flights.size(),
                    "origin", origin,
                    "destination", destination,
                    "departureDate", departureDate.toString(),
                    "flights", flights
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error searching flights: " + e.getMessage()
            ));
        }
    }

    /**
     * Получить детальную информацию о рейсе
     */
    @GetMapping("/{companyId}/{flightId}")
    public ResponseEntity<?> getFlightDetails(
            @PathVariable String companyId,
            @PathVariable Long flightId
    ) {
        try {
            Optional<FlightDTO> flight = flightServiceData.getFlightDetails(companyId, flightId);

            if (flight.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "flight", flight.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error retrieving flight details: " + e.getMessage()
            ));
        }
    }

    /**
     * Добавить новый рейс
     */
    @PostMapping
    public ResponseEntity<?> addFlight(
            @RequestBody FlightDTO flightDTO,
            @RequestHeader(value = "X-Tenant-ID", required = true) String tenantId
    ) {
        try {
            // Валидация входных данных
            if (flightDTO.getFlightNumber() == null || flightDTO.getFlightNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Flight number cannot be empty"
                ));
            }

            if (flightDTO.getOrigin() == null || flightDTO.getOrigin().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Origin city cannot be empty"
                ));
            }

            if (flightDTO.getDestination() == null || flightDTO.getDestination().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Destination city cannot be empty"
                ));
            }

            if (flightDTO.getDepartureTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Departure time cannot be empty"
                ));
            }

            if (flightDTO.getArrivalTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Arrival time cannot be empty"
                ));
            }

            if (flightDTO.getPrice() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Price cannot be empty"
                ));
            }

            if (flightDTO.getAvailableSeats() == null || flightDTO.getAvailableSeats() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Available seats must be greater than zero"
                ));
            }

            if (flightDTO.getAircraft() == null || flightDTO.getAircraft().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Aircraft information cannot be empty"
                ));
            }

            // Проверяем логику дат
            if (flightDTO.getDepartureTime().isAfter(flightDTO.getArrivalTime())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Departure time cannot be after arrival time"
                ));
            }

            // Создаем новый рейс
            FlightDTO savedFlight = flightServiceData.addFlight(flightDTO, tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Flight successfully added");
            response.put("flight", savedFlight);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error adding flight: " + e.getMessage()
            ));
        }
    }

    /**
     * Обновить информацию о рейсе
     */
    @PutMapping("/{flightId}")
    public ResponseEntity<?> updateFlight(
            @PathVariable Long flightId,
            @RequestBody FlightDTO flightDTO,
            @RequestHeader(value = "X-Tenant-ID", required = true) String tenantId
    ) {
        try {
            flightDTO.setId(flightId); // Убедимся, что ID установлен правильно

            FlightDTO updatedFlight = flightServiceData.updateFlight(flightDTO, tenantId);

            if (updatedFlight != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Flight successfully updated",
                        "flight", updatedFlight
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error updating flight: " + e.getMessage()
            ));
        }
    }

    /**
     * Удалить рейс
     */
    @DeleteMapping("/{flightId}")
    public ResponseEntity<?> deleteFlight(
            @PathVariable Long flightId,
            @RequestHeader(value = "X-Tenant-ID", required = true) String tenantId
    ) {
        try {
            boolean deleted = flightServiceData.deleteFlight(flightId, tenantId);

            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Flight successfully deleted"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error deleting flight: " + e.getMessage()
            ));
        }
    }
}