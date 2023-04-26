package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.NameChangeDTO;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Category;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.ModuleEntity;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;
import se.onlyfin.onlyfinbackend.repository.DashboardRepository;
import se.onlyfin.onlyfinbackend.repository.CategoryRepository;
import se.onlyfin.onlyfinbackend.repository.ModuleRepository;
import se.onlyfin.onlyfinbackend.repository.StockRepository;


import java.util.Optional;

@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@RestController
@RequestMapping("/studio")
public class StudioController {

    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final DashboardRepository dashboardRepository;

    public StudioController(StockRepository stockRepository,
                            CategoryRepository categoryRepository,
                            ModuleRepository moduleRepository,
                            DashboardRepository dashboardRepository)
    {
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.moduleRepository = moduleRepository;
        this.dashboardRepository = dashboardRepository;
    }

    @PostMapping("/createStock")
    public String createStock(@RequestBody Stock stock){
            stockRepository.save(stock);
            return "stock added successfully";
    }

    @DeleteMapping("/deleteStock/{id}")
    public String deleteStock(@PathVariable String id) {

        int intId = Integer.parseInt(id);
        if(!stockRepository.existsById(intId)) {
            return "There is no stock with that id";
        }
        stockRepository.deleteById(intId);
        return "Removed stock successfully";
    }

    @PostMapping("/createCategory")
    public ResponseEntity<?> createCategory(@RequestBody Category category){

        if(!stockRepository.existsById(category.getStock_id())){
            return ResponseEntity.badRequest().body("there is no stock for that id");
        }
        categoryRepository.save(category);
        return ResponseEntity.ok(categoryRepository.getReferenceById(category.getId()));
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
            System.out.println(e);
        }
        return "Could not remove category";
    }

    @PutMapping("/updateCategoryName")
    public ResponseEntity<?> updateCategoryName(@RequestBody NameChangeDTO nameChangeRequest){

        Category category;

        if(categoryRepository.existsById(nameChangeRequest.getId())){
            category = categoryRepository.getReferenceById(nameChangeRequest.getId());
            category.setName(nameChangeRequest.getName());
            categoryRepository.save(category);
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.badRequest().body("category id does not exist");
    }

    @PostMapping("/createModule")
    public ResponseEntity<?> createModule(@RequestBody ModuleEntity moduleToSave){

        if(!categoryRepository.existsById(moduleToSave.getCategory_id())){
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
    public ResponseEntity<?> getStocksAndCategories(@PathVariable Integer id){
        Dashboard dashboard;
        if (dashboardRepository.existsById(id)) {
            Optional<Dashboard> dashboardOptional= dashboardRepository.findById(id);
            dashboard = dashboardOptional.orElse(null);
            for (int i = 0; i < dashboard.getStocks().size(); i++){
                for (int j = 0; j < dashboard.getStocks().get(i).getCategories().size(); j++){
                    dashboard.getStocks().get(i).getCategories().get(j).setModuleEntities(null);
                }
            }
            return ResponseEntity.ok(dashboard);
        }
        return ResponseEntity.badRequest().body("cant find dashboard");
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
