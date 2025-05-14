package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.Service.TenantService;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightViewController {

    private final FlightServiceData flightServiceData;
    private final TenantService tenantService;

    /**
     * Просмотр всех рейсов из всех авиакомпаний
     */
    @GetMapping
    public String getAllFlights(Model model) {
        try {
            List<FlightDTO> flights = flightServiceData.getAllFlights();
            model.addAttribute("flights", flights);
            model.addAttribute("title", "All Flights");
            return "flights/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving flights: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Просмотр рейсов конкретной авиакомпании
     */
    @GetMapping("/company/{companyId}")
    public String getCompanyFlights(@PathVariable String companyId, Model model) {
        try {
            List<FlightDTO> flights = flightServiceData.getCompanyFlights(companyId);
            model.addAttribute("flights", flights);

            // Получаем информацию о компании для отображения
            Tenant tenant = tenantService.getTenantById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            model.addAttribute("company", tenant);
            model.addAttribute("title", "Flights - " + tenant.getDescription());
            return "flights/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving flights for company " + companyId + ": " + e.getMessage());
            return "error";
        }
    }

    /**
     * Страница поиска рейсов
     */
    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("title", "Search Flights");
        return "flights/search";
    }

    /**
     * Результаты поиска рейсов
     */
    @PostMapping("/search")
    public String searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            Model model
    ) {
        try {
            // Преобразуем LocalDate в LocalDateTime (начало и конец дня)
            LocalDateTime startOfDay = departureDate.atStartOfDay();
            LocalDateTime endOfDay = departureDate.atTime(LocalTime.MAX);

            List<FlightDTO> flights = flightServiceData.searchFlights(origin, destination, startOfDay, endOfDay);
            model.addAttribute("flights", flights);
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("departureDate", departureDate);
            model.addAttribute("title", "Search Results");
            return "flights/search-results";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching flights: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Просмотр детальной информации о рейсе
     */
    @GetMapping("/{companyId}/{flightId}")
    public String getFlightDetails(
            @PathVariable String companyId,
            @PathVariable Long flightId,
            Model model
    ) {
        try {
            flightServiceData.getFlightDetails(companyId, flightId)
                    .ifPresentOrElse(
                            flight -> {
                                model.addAttribute("flight", flight);
                                model.addAttribute("title", "Flight Details - " + flight.getFlightNumber());
                            },
                            () -> {
                                model.addAttribute("error", "Flight not found");
                                model.addAttribute("title", "Error");
                            }
                    );
            return "flights/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving flight details: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Страница для добавления нового рейса
     */
    @GetMapping("/add")
    public String addFlightForm(Model model) {
        model.addAttribute("flight", new FlightDTO());
        model.addAttribute("companies", tenantService.getAllTenants());
        model.addAttribute("title", "Add New Flight");
        return "flights/add";
    }

    /**
     * Обработка формы добавления рейса
     */
    @PostMapping("/add")
    public String addFlight(@ModelAttribute FlightDTO flightDTO, @RequestParam String tenantId, Model model) {
        try {
            FlightDTO savedFlight = flightServiceData.addFlight(flightDTO, tenantId);
            return "redirect:/flights/" + tenantId + "/" + savedFlight.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error adding flight: " + e.getMessage());
            model.addAttribute("flight", flightDTO);
            model.addAttribute("companies", tenantService.getAllTenants());
            model.addAttribute("title", "Add New Flight");
            return "flights/add";
        }
    }
}