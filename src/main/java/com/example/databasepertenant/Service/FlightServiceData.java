package com.example.databasepertenant.Service;
import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.FlightRepository;
import com.example.databasepertenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceData {
    private final Map<String, FlightRepository> flightRepositories;
    private final FlightMapper flightMapper;
    private final TenantRepository tenantRepository;

    /**
     * Получить все рейсы из всех баз данных компаний
     */
    public List<FlightDTO> getAllFlights() {
        List<FlightDTO> allFlights = new ArrayList<>();

        for (Map.Entry<String, FlightRepository> entry : flightRepositories.entrySet()) {
            String companyId = entry.getKey();

            // Устанавливаем текущего тенанта перед выполнением запросов
            TenantContext.setCurrentTenant(companyId);

            try {
                List<Flight> flights = entry.getValue().findAll();
                List<FlightDTO> companyFlights = flightMapper.toDtoList(flights, companyId);
                allFlights.addAll(companyFlights);
            } finally {
                // Очищаем контекст тенанта после выполнения
                TenantContext.clear();
            }
        }

        return allFlights;
    }

    public List<FlightDTO> getCompanyFlights(String companyId) {
        FlightRepository repository = flightRepositories.get(companyId);
        if (repository == null) {
            throw new RuntimeException("Компания не найдена: " + companyId);
        }

        // Устанавливаем текущего тенанта перед выполнением запросов
        TenantContext.setCurrentTenant(companyId);

        try {
            List<Flight> flights = repository.findAll();
            return flightMapper.toDtoList(flights, companyId);
        } finally {
            // Очищаем контекст тенанта после выполнения
            TenantContext.clear();
        }
    }
    
    /**
     * Найти рейсы по параметрам поиска
     */
    public List<FlightDTO> searchFlights(String origin, String destination, LocalDateTime departureDate) {
        List<FlightDTO> matchingFlights = new ArrayList<>();

        for (Map.Entry<String, FlightRepository> entry : flightRepositories.entrySet()) {
            String companyId = entry.getKey();
            FlightRepository repository = entry.getValue();

            TenantContext.setCurrentTenant(companyId);

            try {
                // Предполагается, что вы добавите метод поиска в репозиторий
                // Здесь для примера используем фильтрацию в памяти
                List<Flight> flights = repository.findAll();

                List<FlightDTO> companyMatches = flights.stream()
                        .filter(flight -> flight.getOrigin().equals(origin))
                        .filter(flight -> flight.getDestination().equals(destination))
                        .filter(flight -> isSameDay(flight.getDepartureTime(), departureDate))
                        .map(flight -> flightMapper.toDto(flight, companyId))
                        .collect(Collectors.toList());

                matchingFlights.addAll(companyMatches);
            } finally {
                TenantContext.clear();
            }
        }

        return matchingFlights;
    }

    private boolean isSameDay(LocalDateTime time1, LocalDateTime time2) {
        return time1.getYear() == time2.getYear() &&
                time1.getMonth() == time2.getMonth() &&
                time1.getDayOfMonth() == time2.getDayOfMonth();
    }

    /**
     * Получить детальную информацию о рейсе
     */
    public Optional<FlightDTO> getFlightDetails(String companyId, Long flightId) {
        FlightRepository repository = flightRepositories.get(companyId);
        if (repository == null) {
            throw new RuntimeException("Компания не найдена: " + companyId);
        }

        TenantContext.setCurrentTenant(companyId);

        try {
            return repository.findById(flightId)
                    .map(flight -> flightMapper.toDto(flight, companyId));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Добавить новый рейс
     */
    public FlightDTO addFlight(FlightDTO flightDTO, String companyId) {
        FlightRepository repository = flightRepositories.get(companyId);
        if (repository == null) {
            throw new RuntimeException("Компания не найдена: " + companyId);
        }

        TenantContext.setCurrentTenant(companyId);

        try {
            Flight flight = flightMapper.toEntity(flightDTO);
            Flight savedFlight = repository.save(flight);
            return flightMapper.toDto(savedFlight, companyId);
        } finally {
            TenantContext.clear();
        }
    }
}