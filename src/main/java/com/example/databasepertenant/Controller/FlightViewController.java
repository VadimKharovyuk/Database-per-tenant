package com.example.databasepertenant.controller;

import com.example.databasepertenant.Service.FlightServiceData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightViewController {

    private final FlightServiceData flightServiceData;

    @GetMapping
    public String getAllFlights(Model model) {
        model.addAttribute("flights", flightServiceData.getAllFlights());
        return "flight-list";
    }

    @GetMapping("/company/{companyId}")
    public String getCompanyFlights(@PathVariable String companyId, Model model) {
        model.addAttribute("flights", flightServiceData.getCompanyFlights(companyId));
        model.addAttribute("companyId", companyId);
        return "company-flights";
    }
}