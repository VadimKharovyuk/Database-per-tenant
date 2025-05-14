package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceData {
    private final Map<String, FlightRepository> flightRepositories;
    private final FlightMapper flightMapper;

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
                allFlights.addAll(flightMapper.toDtoList(flights, companyId));
            } finally {
                // Очищаем контекст тенанта после выполнения
                TenantContext.clear();
            }
        }

        return allFlights;
    }

    /**
     * Получить рейсы конкретной авиакомпании
     */
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
     * Поиск рейсов по параметрам
     */
    public List<FlightDTO> searchFlights(
            String origin,
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<FlightDTO> matchingFlights = new ArrayList<>();

        for (Map.Entry<String, FlightRepository> entry : flightRepositories.entrySet()) {
            String companyId = entry.getKey();
            FlightRepository repository = entry.getValue();

            TenantContext.setCurrentTenant(companyId);

            try {
                List<Flight> flights = repository.findByOriginAndDestinationAndDepartureTimeBetween(
                        origin, destination, startDate, endDate);

                matchingFlights.addAll(flightMapper.toDtoList(flights, companyId));
            } finally {
                TenantContext.clear();
            }
        }

        return matchingFlights;
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

    /**
     * Обновить информацию о рейсе
     */
    public FlightDTO updateFlight(FlightDTO flightDTO, String companyId) {
        FlightRepository repository = flightRepositories.get(companyId);
        if (repository == null) {
            throw new RuntimeException("Компания не найдена: " + companyId);
        }

        if (flightDTO.getId() == null) {
            throw new IllegalArgumentException("Flight ID cannot be null for update operation");
        }

        TenantContext.setCurrentTenant(companyId);

        try {
            // Проверяем, существует ли рейс
            if (!repository.existsById(flightDTO.getId())) {
                return null; // Рейс не найден
            }

            Flight flight = flightMapper.toEntity(flightDTO);
            Flight updatedFlight = repository.save(flight);
            return flightMapper.toDto(updatedFlight, companyId);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Удалить рейс
     */
    public boolean deleteFlight(Long flightId, String companyId) {
        FlightRepository repository = flightRepositories.get(companyId);
        if (repository == null) {
            throw new RuntimeException("Компания не найдена: " + companyId);
        }

        TenantContext.setCurrentTenant(companyId);

        try {
            // Проверяем, существует ли рейс
            if (!repository.existsById(flightId)) {
                return false; // Рейс не найден
            }

            repository.deleteById(flightId);
            return true;
        } finally {
            TenantContext.clear();
        }
    }
}