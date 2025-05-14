package com.example.databasepertenant.maper;

import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.model.Flight;
import com.example.databasepertenant.repository.TenantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlightMapper {

    private final TenantRepository tenantRepository;

    public FlightMapper(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Преобразует сущность Flight в DTO
     */
    public FlightDTO toDto(Flight flight, String companyId) {
        if (flight == null) {
            return null;
        }

        FlightDTO dto = new FlightDTO();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setOrigin(flight.getOrigin());
        dto.setDestination(flight.getDestination());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setPrice(flight.getPrice());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setAircraft(flight.getAircraft());
        dto.setCompanyId(companyId);

        // Находим название компании
        tenantRepository.findById(companyId).ifPresent(
                tenant -> dto.setCompanyName(tenant.getDescription())
        );

        return dto;
    }

    /**
     * Преобразует список сущностей Flight в список DTO
     */
    public List<FlightDTO> toDtoList(List<Flight> flights, String companyId) {
        if (flights == null) {
            return List.of();
        }

        return flights.stream()
                .map(flight -> toDto(flight, companyId))
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO в сущность Flight
     */
    public Flight toEntity(FlightDTO dto) {
        if (dto == null) {
            return null;
        }

        Flight flight = new Flight();
        flight.setId(dto.getId());
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setOrigin(dto.getOrigin());
        flight.setDestination(dto.getDestination());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setPrice(dto.getPrice());
        flight.setAvailableSeats(dto.getAvailableSeats());
        flight.setAircraft(dto.getAircraft());
        return flight;
    }

    /**
     * Преобразует список DTO в список сущностей Flight
     */
    public List<Flight> toEntityList(List<FlightDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}