package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantAwareDataSource;
import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.model.Tenant;
import com.example.databasepertenant.repository.FlightRepository;
import com.example.databasepertenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static jakarta.persistence.Persistence.createEntityManagerFactory;
@Slf4j
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

        System.out.println("Доступно репозиториев: " + flightRepositories.size());
        flightRepositories.keySet().forEach(key -> System.out.println("Ключ в репозиториях: " + key));

        for (Map.Entry<String, FlightRepository> entry : flightRepositories.entrySet()) {
            String companyId = entry.getKey();
            System.out.println("Обрабатываем компанию: " + companyId);

            // Устанавливаем текущего тенанта перед выполнением запросов
            TenantContext.setCurrentTenant(companyId);

            try {
                List<Flight> flights = entry.getValue().findAll();
                System.out.println("Получено рейсов для компании " + companyId + ": " + flights.size());
                allFlights.addAll(flightMapper.toDtoList(flights, companyId));
            } catch (Exception e) {
                // Логируем ошибку, но продолжаем обработку других репозиториев
                System.err.println("Ошибка при получении рейсов для компании " + companyId + ": " + e.getMessage());
                // Удаляем проблемный репозиторий из карты
                flightRepositories.remove(companyId);
                System.out.println("Репозиторий для компании " + companyId + " удален из-за ошибки");
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

    public Flight getFlightEntity(Long flightId, String tenantId) {
        // Получаем репозиторий для конкретного тенанта
        FlightRepository repository = flightRepositories.get(tenantId);

        if (repository == null) {
            throw new RuntimeException("Репозиторий для тенанта не найден: " + tenantId);
        }

        // Устанавливаем контекст тенанта
        TenantContext.setCurrentTenant(tenantId);

        try {
            // Ищем рейс по ID
            return repository.findById(flightId)
                    .orElseThrow(() -> new RuntimeException("Рейс не найден: ID " + flightId + " в тенанте " + tenantId));
        } finally {
            // Всегда очищаем контекст тенанта
            TenantContext.clear();
        }
    }

    public Optional<FlightDTO> getFlightById(Long flightId) {
        // Получаем текущий tenant ID
        String currentTenantId = TenantContext.getCurrentTenant();

        if (currentTenantId == null) {
            throw new RuntimeException("Tenant ID не определен");
        }

        // Получаем репозиторий для текущего тенанта
        FlightRepository repository = flightRepositories.get(currentTenantId);

        if (repository == null) {
            throw new RuntimeException("Репозиторий для тенанта не найден: " + currentTenantId);
        }

        try {
            return repository.findById(flightId)
                    .map(flight -> flightMapper.toDto(flight, currentTenantId));
        } catch (Exception e) {
            // Логирование ошибки
            log.error("Ошибка при поиске рейса", e);
            throw new RuntimeException("Ошибка при поиске рейса", e);
        } finally {
            // Очищаем контекст тенанта
            TenantContext.clear();
        }
    }
}