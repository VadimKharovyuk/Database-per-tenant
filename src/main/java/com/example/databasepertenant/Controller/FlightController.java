package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightServiceData flightServiceData;

    /**
     * Получить все рейсы из всех авиакомпаний
     */
    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        return ResponseEntity.ok(flightServiceData.getAllFlights());
    }

    /**
     * Получить рейсы конкретной авиакомпании
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<FlightDTO>> getCompanyFlights(@PathVariable String companyId) {
        try {
            return ResponseEntity.ok(flightServiceData.getCompanyFlights(companyId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Поиск рейсов по параметрам
     */
    @GetMapping("/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDate
    ) {
        return ResponseEntity.ok(flightServiceData.searchFlights(origin, destination, departureDate));
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
            return flightServiceData.getFlightDetails(companyId, flightId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при получении информации о рейсе: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
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
            FlightDTO savedFlight = flightServiceData.addFlight(flightDTO, tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Рейс успешно добавлен");
            response.put("flight", savedFlight);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Ошибка при добавлении рейса: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}