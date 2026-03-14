package com.dattakrupa.laundry.controller;

import com.dattakrupa.laundry.dto.ApiResponseDTO;
import com.dattakrupa.laundry.dto.DashboardResponseDTO;
import com.dattakrupa.laundry.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private  DashboardService dashboardService;

    // ── GET /dashboard ── Owner ka poora dashboard data
    @GetMapping
    public ResponseEntity<ApiResponseDTO<DashboardResponseDTO>> getDashboard() {

        DashboardResponseDTO data = dashboardService.getDashboardData();
        return ResponseEntity.ok(
                ApiResponseDTO.success("DattaKrupa Laundry Dashboard", data));
    }
}