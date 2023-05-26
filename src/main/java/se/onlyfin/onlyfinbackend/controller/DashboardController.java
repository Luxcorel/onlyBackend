package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.DashboardWithLayoutDTO;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;
import se.onlyfin.onlyfinbackend.repository.DashboardLayoutRepository;
import se.onlyfin.onlyfinbackend.repository.DashboardRepository;
import se.onlyfin.onlyfinbackend.repository.StockRefRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for providing access to dashboards.
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = {"https://onlyfrontend-production.up.railway.app", "https://beta.onlyfin.se"}, allowCredentials = "true")
public class DashboardController {
    private final DashboardRepository dashboardRepository;
    private final StockRefRepository stockRefRepository;
    private final DashboardLayoutRepository dashboardLayoutRepository;

    public DashboardController(DashboardRepository dashboardRepository,
                               StockRefRepository stockRefRepository,
                               DashboardLayoutRepository dashboardLayoutRepository) {
        this.dashboardRepository = dashboardRepository;
        this.stockRefRepository = stockRefRepository;
        this.dashboardLayoutRepository = dashboardLayoutRepository;
    }

    /**
     * Fetches a dashboard with layout using a target id
     *
     * @param id target dashboard's id
     * @return dashboard with layout
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<DashboardWithLayoutDTO> getDashboard(@PathVariable Integer id) {
        Optional<Dashboard> optionalDashboard = dashboardRepository.findById(id);
        Dashboard dashboard = optionalDashboard.orElse(null);
        if (dashboard == null) {
            System.out.println("is null");
            return ResponseEntity.notFound().build();
        }

        List<DashboardLayout> layoutList = new ArrayList<>();

        for (int i = 0; i < dashboard.getStocks().size(); i++) {
            for (int j = 0; j < dashboard.getStocks().get(i).getCategories().size(); j++) {

                int tempCategoryId = dashboard.getStocks().get(i).getCategories().get(j).getId();
                List<DashboardLayout> tempList = dashboardLayoutRepository.findByCategoryId(tempCategoryId);
                layoutList.addAll(tempList);
            }
        }

        DashboardWithLayoutDTO dashboardToSend = new DashboardWithLayoutDTO(dashboard, layoutList);

        return ResponseEntity.ok(dashboardToSend);
    }

    /**
     * Returns a list of all stock references.
     *
     * @return list of all stock references
     */
    @GetMapping("/getStockRef")
    public ResponseEntity<List<StockRef>> getStockRef() {
        List<StockRef> stockRefs = stockRefRepository.findAll();

        return ResponseEntity.ok(stockRefs);
    }

    /**
     * Returns the dashboard object for a user specified by user id
     *
     * @param userId id of user to target
     * @return dashboard object or null if no dashboard is found
     */
    public Dashboard fetchDashboardOrNull(Integer userId) {
        return dashboardRepository.findById(userId).orElse(null);
    }

    /**
     * Returns a map which contains what stocks are covered by which analysts
     *
     * @param analysts analysts to include
     * @return map of stocks and who covers them
     */
    public HashMap<StockRef, ArrayList<User>> createCoverageMap(List<User> analysts) {
        HashMap<StockRef, ArrayList<User>> coverageMap = new HashMap<>();

        ArrayList<User> analystList = new ArrayList<>(analysts);
        if (analystList.isEmpty()) {
            return coverageMap;
        }

        for (User currentAnalyst : analystList) {
            Dashboard currentDashboard = fetchDashboardOrNull(currentAnalyst.getId());
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
