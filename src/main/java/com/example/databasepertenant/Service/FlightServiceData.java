package com.example.databasepertenant.Service;

import com.example.databasepertenant.DataSource.TenantContext;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.maper.FlightMapper;
import com.example.databasepertenant.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceData {
    private final Map<String, FlightRepository> flightRepositories;
    private final FlightMapper flightMapper;

    public List<FlightDTO> getAllFlights() {
        List<FlightDTO> allFlights = new ArrayList<>();

        for (Map.Entry<String, FlightRepository> entry : flightRepositories.entrySet()) {
            String companyId = entry.getKey();
            // Устанавливаем текущего тенанта перед выполнением запросов
            TenantContext.setCurrentTenant(companyId);

            try {
                List<FlightDTO> companyFlights = entry.getValue().findAll()
                        .stream()
                        .map(flightMapper::toDto)
                        .collect(Collectors.toList());

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
            return repository.findAll()
                    .stream()
                    .map(flightMapper::toDto)
                    .collect(Collectors.toList());
        } finally {
            // Очищаем контекст тенанта после выполнения
            TenantContext.clear();
        }
    }
}