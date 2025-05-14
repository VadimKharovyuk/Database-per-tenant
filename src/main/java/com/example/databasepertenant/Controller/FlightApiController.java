package com.example.databasepertenant.Controller;

import com.example.databasepertenant.Service.FlightServiceData;
import com.example.databasepertenant.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightApiController {

    private final FlightServiceData flightServiceData;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        return ResponseEntity.ok(flightServiceData.getAllFlights());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<FlightDTO>> getCompanyFlights(@PathVariable String companyId) {
        return ResponseEntity.ok(flightServiceData.getCompanyFlights(companyId));
    }
}