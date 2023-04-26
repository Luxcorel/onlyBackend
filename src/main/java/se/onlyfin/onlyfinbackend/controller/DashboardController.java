package se.onlyfin.onlyfinbackend.controller;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.*;
import se.onlyfin.onlyfinbackend.repository.DashboardRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "https://onlybackend-production.up.railway.app", allowCredentials = "true")
public class DashboardController {

    private DashboardRepository dashboardRepository;

    public DashboardController(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable Integer id) {
        Optional<Dashboard> optionalDashboard = dashboardRepository.findById(id);
        Dashboard dashboard = optionalDashboard.orElse(null);
        if (dashboard == null) {
            System.out.println("is null");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dashboard);
    }

    public Instant fetchAnalystsLastPostTime(@NonNull User targetAnalyst) {
        Dashboard targetAnalystsDashboard = dashboardRepository.findById(targetAnalyst.getId()).orElseThrow();

        List<Stock> analystsStocks = targetAnalystsDashboard.getStocks();
        Instant latestInstant = Instant.MIN;
        for (Stock currentStock : analystsStocks) {
            for (Category currentCategoryUnderStock : currentStock.getCategories()) {
                for (ModuleEntity currentModuleUnderCategory : currentCategoryUnderStock.getModuleEntities()) {
                    Instant currentInstant = currentModuleUnderCategory.getPostDate();
                    if (currentInstant.isAfter(latestInstant)) {
                        latestInstant = currentInstant;
                    }
                }
            }
        }

        return latestInstant;
    }

    public Instant fetchAnalystsLastUpdateTime(@NonNull User targetAnalyst) {
        Dashboard targetAnalystsDashboard = dashboardRepository.findById(targetAnalyst.getId()).orElseThrow();

        List<Stock> analystsStocks = targetAnalystsDashboard.getStocks();
        Instant latestInstant = Instant.MIN;
        for (Stock currentStock : analystsStocks) {
            for (Category currentCategoryUnderStock : currentStock.getCategories()) {
                for (ModuleEntity currentModuleUnderCategory : currentCategoryUnderStock.getModuleEntities()) {
                    Instant currentInstant = currentModuleUnderCategory.getUpdatedDate();
                    if (currentInstant.isAfter(latestInstant)) {
                        latestInstant = currentInstant;
                    }
                }
            }
        }

        return latestInstant;
    }

    public Dashboard fetchDashboard(Integer userId) {
        return dashboardRepository.findById(userId).orElse(null);
    }

    public HashMap<StockRef, ArrayList<User>> fetchCoverageMap(List<User> analysts) {
        HashMap<StockRef, ArrayList<User>> coverageMap = new HashMap<>();

        ArrayList<User> analystList = new ArrayList<>(analysts);

        if (analystList.isEmpty()) {
            return coverageMap;
        }

        for (User currentAnalyst : analystList) {
            Dashboard currentDashboard = fetchDashboard(currentAnalyst.getId());
            if (currentDashboard != null) {
                for (Stock currentStock : currentDashboard.getStocks()) {
                    StockRef currentStockRef = currentStock.getStock_ref_id();
                    if (!coverageMap.containsKey(currentStockRef)) {
                        coverageMap.put(currentStockRef, new ArrayList<>());
                    }
                    coverageMap.get(currentStockRef).add(currentAnalyst);
                }
            }
        }

        return coverageMap;

    }

}
