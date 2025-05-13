package com.example.databasepertenant.maper;

import com.example.databasepertenant.dto.CreateFlightDTO;
import com.example.databasepertenant.dto.FlightDTO;
import com.example.databasepertenant.dto.UpdateFlightDTO;
import com.example.databasepertenant.model.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    /**
     * Преобразует объект Flight в FlightDTO
     */
    public FlightDTO toDto(Flight flight) {
        if (flight == null) {
            return null;
        }

        FlightDTO dto = new FlightDTO();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setDepartureAirport(flight.getDepartureAirport());
        dto.setArrivalAirport(flight.getArrivalAirport());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setBasePrice(flight.getBasePrice());

        return dto;
    }

    /**
     * Преобразует CreateFlightDTO в Flight
     */
    public Flight toEntity(CreateFlightDTO dto) {
        if (dto == null) {
            return null;
        }

        Flight flight = new Flight();
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setDepartureAirport(dto.getDepartureAirport());
        flight.setArrivalAirport(dto.getArrivalAirport());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setAvailableSeats(dto.getAvailableSeats());
        flight.setBasePrice(dto.getBasePrice());

        return flight;
    }

    /**
     * Обновляет Flight на основе UpdateFlightDTO
     */
    public void updateFlightFromDto(UpdateFlightDTO dto, Flight flight) {
        if (dto == null || flight == null) {
            return;
        }

        flight.setFlightNumber(dto.getFlightNumber());
        flight.setDepartureAirport(dto.getDepartureAirport());
        flight.setArrivalAirport(dto.getArrivalAirport());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setAvailableSeats(dto.getAvailableSeats());
        flight.setBasePrice(dto.getBasePrice());
    }
}
