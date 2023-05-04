package se.onlyfin.onlyfinbackend.controller;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.*;
import se.onlyfin.onlyfinbackend.repository.DashboardRepository;
import se.onlyfin.onlyfinbackend.repository.StockRefRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
public class DashboardController {

    private final DashboardRepository dashboardRepository;
    private final StockRefRepository stockRefRepository;

    public DashboardController(DashboardRepository dashboardRepository, StockRefRepository stockRefRepository) {
        this.dashboardRepository = dashboardRepository;
        this.stockRefRepository = stockRefRepository;
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

    @GetMapping("/getStockRef")
    public ResponseEntity<?> getStockRef() {
        List<StockRef> stockRefs = stockRefRepository.findAll();

        return ResponseEntity.ok(stockRefs);
    }

    /**
     * This method goes through all the analyst's posts and returns whichever Instant is the latest date
     *
     * @param targetAnalyst the analysts to target
     * @return Instant object containing time&date information about the last post date
     */
    public Instant fetchAnalystsLastPostTime(@NonNull User targetAnalyst) {
        Optional<Dashboard> optionalTargetAnalystsDashboard = dashboardRepository.findById(targetAnalyst.getId());
        if (optionalTargetAnalystsDashboard.isEmpty()) {
            return Instant.MIN;
        }

        Dashboard targetAnalystsDashboard = optionalTargetAnalystsDashboard.get();
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

    /**
     * This method goes through all the analyst's posts and returns whichever Instant is the latest update date
     *
     * @param targetAnalyst the analysts to target
     * @return Instant object containing time&date information about the last post update date
     */
    public Instant fetchAnalystsLastUpdateTime(@NonNull User targetAnalyst) {
        Optional<Dashboard> optionalTargetAnalystsDashboard = dashboardRepository.findById(targetAnalyst.getId());
        if (optionalTargetAnalystsDashboard.isEmpty()) {
            return Instant.MIN;
        }

        Dashboard targetAnalystsDashboard = optionalTargetAnalystsDashboard.get();
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

    /**
     * Returns the dashboard object for a user specified by user id
     *
     * @param userId id of user to target
     * @return dashboard object or null if no dashboard is found
     */
    public Dashboard fetchDashboard(Integer userId) {
        return dashboardRepository.findById(userId).orElse(null);
    }

    /**
     * Returns a map which contains what stocks are covered by which analysts
     *
     * @param analysts analysts to include
     * @return map of stocks and who covers them
     */
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
