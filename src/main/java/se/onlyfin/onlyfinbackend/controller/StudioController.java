package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.NameChangeDT;
import se.onlyfin.onlyfinbackend.DTO.StockRefDTO;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.*;
import se.onlyfin.onlyfinbackend.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@RestController
@RequestMapping("/studio")
public class StudioController {
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final DashboardRepository dashboardRepository;
    private final StockRefRepository stockRefRepository;

    public StudioController(StockRepository stockRepository,
                            CategoryRepository categoryRepository,
                            ModuleRepository moduleRepository,
                            DashboardRepository dashboardRepository,
                            StockRefRepository stockRefRepository) {
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.moduleRepository = moduleRepository;
        this.dashboardRepository = dashboardRepository;
        this.stockRefRepository = stockRefRepository;
    }

    @PostMapping("/createStock")
    public String createStock(@RequestBody StockRefDTO stockRefDTO) {
        StockRef stockRef = stockRefRepository.findById(stockRefDTO.stockRefId()).orElseThrow(() ->
                new NoSuchElementException("Stock ref not found"));

        Stock stockToSave = new Stock();
        stockToSave.setStock_ref_id(stockRef);

        stockToSave.setDashboard_id(new Dashboard(stockRefDTO.dashboardId()));

        stockRepository.save(stockToSave);
        return "stock added successfully";
    }

    @DeleteMapping("/deleteStock/{id}")
    public String deleteStock(@PathVariable String id) {

        int intId = Integer.parseInt(id);
        if (!stockRepository.existsById(intId)) {
            return "There is no stock with that id";
        }
        stockRepository.deleteById(intId);
        return "Removed stock successfully";
    }

    @PostMapping("/createCategory")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {

        if (!stockRepository.existsById(category.getStock_id())) {
            return ResponseEntity.badRequest().body("there is no stock for that id");
        }
        categoryRepository.save(category);
        return ResponseEntity.ok(categoryRepository.getReferenceById(category.getId()));
    }

    /**
     * Creates a new category under a stock specified by id
     *
     * @param stockId           id of the target stock
     * @param nameOfNewCategory name of the new category to be created
     * @return name of new category if successful
     */
    @PostMapping("/createCategoryUsingStockId")
    public ResponseEntity<String> createCategoryUsingStockId(Integer stockId, String nameOfNewCategory) {
        if (nameOfNewCategory.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Stock targetStock = stockRepository.findById(stockId).orElseThrow(() ->
                new NoSuchElementException("No such stock!"));

        List<Category> currentCategories = new ArrayList<>(targetStock.getCategories());

        Category newCategory = new Category();
        newCategory.setName(nameOfNewCategory);
        currentCategories.add(newCategory);

        targetStock.setCategories(currentCategories);
        stockRepository.save(targetStock);

        return ResponseEntity.ok().body(newCategory.getName());
    }

    @DeleteMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable String id) {

        try {
            int intId = Integer.parseInt(id);
            if (!categoryRepository.existsById(intId)) {
                return "There is no category with that id";
            }
            categoryRepository.deleteById(intId);
            return "Removed category successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Could not remove category";
    }

    @PutMapping("/updateCategoryName")
    public ResponseEntity<?> updateCategoryName(@RequestBody NameChangeDT nameChangeRequest) {

        Category category;
        if (categoryRepository.existsById(nameChangeRequest.id())) {

            Optional<Category> optionalCategory = categoryRepository.findById(nameChangeRequest.id());
            category = optionalCategory.orElseThrow();
            category.setName(nameChangeRequest.name());
            categoryRepository.save(category);
            return ResponseEntity.ok().body(category);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/createModule")
    public ResponseEntity<?> createModule(@RequestBody ModuleEntity moduleToSave) {

        if (!categoryRepository.existsById(moduleToSave.getCategory_id())) {
            return ResponseEntity.badRequest().body("there is no category for that id");
        }
        moduleRepository.save(moduleToSave);
        return ResponseEntity.ok(moduleRepository.getReferenceById(moduleToSave.getId()));
    }

    @DeleteMapping("/deleteModule/{id}")
    public String deleteModule(@PathVariable Integer id) {

        if (!moduleRepository.existsById(id)) {
            return "There is no module with that id";
        }
        moduleRepository.deleteById(id);
        return "Removed module successfully";
    }

    @GetMapping("getStocksAndCategories/{id}")
    public ResponseEntity<?> getStocksAndCategories(@PathVariable Integer id) {
        Dashboard dashboard;
        if (dashboardRepository.existsById(id)) {
            Optional<Dashboard> dashboardOptional = dashboardRepository.findById(id);
            dashboard = dashboardOptional.orElseThrow();
            for (int i = 0; i < dashboard.getStocks().size(); i++) {
                for (int j = 0; j < dashboard.getStocks().get(i).getCategories().size(); j++) {
                    dashboard.getStocks().get(i).getCategories().get(j).setModuleEntities(null);
                }
            }
            return ResponseEntity.ok(dashboard);
        }
        return ResponseEntity.badRequest().body("cant find dashboard");
    }

    @GetMapping("getModuleFromId/{id}")
    public ResponseEntity<ModuleEntity> getModuleFromEntity(@PathVariable Integer id) {
        System.out.println(id);

        if (moduleRepository.existsById(id)) {
            Optional<ModuleEntity> moduleOptional = moduleRepository.findById(id);
            ModuleEntity module = moduleOptional.orElse(null);
            return ResponseEntity.ok(module);
        } else return ResponseEntity.badRequest().build();
    }

    /*@PutMapping("/updateModuleContent")
    public ResponseEntity<?> updateModuleContent(@RequestBody ContentChangeRequest ccr){

        ModuleEntity module;

        if(moduleRepository.existsById(ccr.getId())){
            module = moduleRepository.getReferenceById(ccr.getId());
            module.setContent(ccr.getContent());
            moduleRepository.save(module);
            return ResponseEntity.ok(module);
        }
        return ResponseEntity.badRequest().body("module id does not exist");
    }*/
}
