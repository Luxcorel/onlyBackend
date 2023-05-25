package se.onlyfin.onlyfinbackend.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.CategoryNameChangeDTO;
import se.onlyfin.onlyfinbackend.DTO.LayoutDTO;
import se.onlyfin.onlyfinbackend.DTO.StockRefDTO;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.*;
import se.onlyfin.onlyfinbackend.repository.*;
import se.onlyfin.onlyfinbackend.service.UserService;

import java.awt.print.Printable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for handling requests related to the graph creation studio.
 */
@CrossOrigin(origins = {"https://onlyfrontend-production.up.railway.app", "https://onlyfin.se"}, allowCredentials = "true")
@RestController
@RequestMapping("/studio")
public class StudioController {
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final DashboardRepository dashboardRepository;
    private final StockRefRepository stockRefRepository;
    private final DashboardLayoutRepository dashboardLayoutRepository;
    private final UserService userService;

    public StudioController(StockRepository stockRepository,
                            CategoryRepository categoryRepository,
                            ModuleRepository moduleRepository,
                            DashboardRepository dashboardRepository,
                            StockRefRepository stockRefRepository,
                            DashboardLayoutRepository dashboardLayoutRepository,
                            UserService userService) {
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.moduleRepository = moduleRepository;
        this.dashboardRepository = dashboardRepository;
        this.stockRefRepository = stockRefRepository;
        this.dashboardLayoutRepository = dashboardLayoutRepository;
        this.userService = userService;
    }

    /**
     * Adds a stock to a specific dashboard
     *
     * @param stockRefDTO DTO containing stock reference id and target dashboard's id
     * @return HTTP 200 if successful
     */
    @PostMapping("/createStock")
    public ResponseEntity<String> createStock(@RequestBody StockRefDTO stockRefDTO, Principal principal) {
        User targetUser = userService.getUserOrException(principal.getName());

        StockRef stockRef = stockRefRepository.findById(stockRefDTO.stockRefId()).orElse(null);

        Stock stockToSave = new Stock();
        stockToSave.setStock_ref_id(stockRef);
        stockToSave.setDashboard_id(new Dashboard(stockRefDTO.dashboardId()));

        if(stockToSave.getDashboard_id() == targetUser.getId()){
            stockRepository.save(stockToSave);
            return ResponseEntity.ok().body("stock added successfully");
        }
        else {
            return ResponseEntity.badRequest().body("user not allowed to change");
        }

    }

    /**
     * Deletes a specified stock by id.
     * Will also delete categories and modules under the stock.
     *
     * @param id target stock id
     * @return HTTP 200 if successful
     */
    @DeleteMapping("/deleteStock/{id}")

    public ResponseEntity<String> deleteStock(@PathVariable Integer id, Principal principal) {
        User targetUser = userService.getUserOrException(principal.getName());

        if (!stockRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("There is no stock with that id");
        }


            stockRepository.deleteById(id);
            return ResponseEntity.ok().body("Removed stock successfully");
    }

    /**
     * Creates a category under a target stock
     *
     * @param category (target stock id and name of category)
     * @return HTTP 200 if successful
     */
    @PostMapping("/createCategory")
    public ResponseEntity<?> createCategory(@RequestBody Category category, Principal principal) {
        int targetStockId = category.getStock_id();

        if (!stockRepository.existsById(targetStockId)) {
            return ResponseEntity.badRequest().body("there is no stock for that id");
        }

        //if(stockRepository.findById(targetStockId).orElse(null).getDashboard_id() == );

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    /**
     * Deletes a specified category by id.
     * Will also delete modules under the category.
     *
     * @param id target category id
     * @return HTTP 200 if successful
     */
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("There is no category with that id");
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.ok().body("Removed category successfully");
    }

    /**
     * Updates an existing category.
     *
     * @param nameChangeRequest DTO containing new name and the target category id
     * @return the updated category if successful
     */
    @PutMapping("/updateCategoryName")
    public ResponseEntity<Category> updateCategoryName(@RequestBody CategoryNameChangeDTO nameChangeRequest) {
        int targetCategoryId = nameChangeRequest.id();

        Category targetCategory = categoryRepository.findById(targetCategoryId).orElse(null);
        if (targetCategory == null) {
            return ResponseEntity.badRequest().build();
        }

        targetCategory.setName(nameChangeRequest.name());

        Category savedCategory = categoryRepository.save(targetCategory);
        return ResponseEntity.ok().body(savedCategory);
    }

    /**
     * Creates a new module under a target category (which is under a stock)
     *
     * @param moduleToSave (target category id, module type, raw JSON content)
     * @return the saved module if successful
     */
    @PostMapping("/createModule")
    public ResponseEntity<?> createModule(@RequestBody ModuleEntity moduleToSave, Principal principal) {
        User targetUser = userService.getUserOrException(principal.getName());
        int targetCategoryId = moduleToSave.getCategory_id();

        // checks to see if the categoryId exists
        if (!categoryRepository.existsById(targetCategoryId)) {
            return ResponseEntity.badRequest().body("there is no category for that id");
        }

        //checks to see if its your own category youre trying to save it to
        if (categoryRepository.findDashboardFromCategoryId(targetCategoryId) == targetUser.getId()) {
            ModuleEntity savedModule = moduleRepository.save(moduleToSave);
            DashboardLayout moduleDashboardLayout = new DashboardLayout(savedModule.getId(), savedModule.getCategory_id());
            dashboardLayoutRepository.save(moduleDashboardLayout);

            return ResponseEntity.ok(savedModule);
        } else {

            return ResponseEntity.badRequest().body("youre not owner of categoryID");
        }
    }

    /**
     * Deletes a specified module by its id.
     *
     * @param id the target module id
     * @return HTTP 200 if successful
     */
    @DeleteMapping("/deleteModule/{id}")
    public ResponseEntity<String> deleteModule(@PathVariable Integer id, Principal principal) {
        User targetUser = userService.getUserOrException(principal.getName());

        if (!moduleRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("There is no module with that id");
        }

        if (moduleRepository.findDashboardByModuleId(id).getId() == targetUser.getId()) {
            moduleRepository.deleteById(id);
            return ResponseEntity.ok().body("Removed module successfully");
        } else {
            return ResponseEntity.badRequest().body("not your module");
        }

    }

    /**
     * Fetches a module by its id
     *
     * @param id id of target module
     * @return module object if it exists
     */
    @GetMapping("/getModuleFromId/{id}")
    public ResponseEntity<ModuleEntity> getModuleById(@PathVariable Integer id) {
        ModuleEntity targetModuleEntity = moduleRepository.findById(id).orElse(null);
        if (targetModuleEntity == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(targetModuleEntity);
    }

    /**
     * Updates an existing module by its id.
     *
     * @param module update module (target category id, module type, raw JSON content)
     * @return Updated module if successful
     */
    @PutMapping("/updateModuleContent")
    public ResponseEntity<?> updateModuleContent(@RequestBody ModuleEntity module, Principal principal) {
        User targetUser = userService.getUserOrException(principal.getName());
        ModuleEntity moduleToUpdate = moduleRepository.findById(module.getId()).orElse(null);

        // checks to see if the module exists
        if (moduleToUpdate == null) {
            return ResponseEntity.badRequest().body("module id does not exist");
        }

        // checks to see if you own the module youre trying to update.
        if (moduleRepository.findDashboardByModuleId(module.getId()).getId() == targetUser.getId()) {
            moduleToUpdate.setContent(module.getContent());

            ModuleEntity savedModule = moduleRepository.save(moduleToUpdate);
            return ResponseEntity.ok(savedModule);
        }

        // if youre not the owner of the module
        else {
            return ResponseEntity.badRequest().body("not your module");
        }


    }

    /**
     * Fetches all stocks and categories under a target dashboard, excluding the content.
     *
     * @param id the target dashboard's id
     * @return dashboard with stocks and categories
     */
    @GetMapping("/getStocksAndCategories/{id}")
    public ResponseEntity<?> getStocksAndCategories(@PathVariable Integer id) {
        Dashboard targetDashboard = dashboardRepository.findById(id).orElse(null);
        if (targetDashboard == null) {
            return ResponseEntity.badRequest().body("cant find dashboard");
        }

        for (int i = 0; i < targetDashboard.getStocks().size(); i++) {
            for (int j = 0; j < targetDashboard.getStocks().get(i).getCategories().size(); j++) {
                targetDashboard.getStocks().get(i).getCategories().get(j).setModuleEntities(null);
            }
        }

        return ResponseEntity.ok(targetDashboard);
    }

    /**
     * Adds one or more dashboard layouts to the database
     *
     * @param layoutDTOList dashboard layout(s) to add to the database
     * @return added dashboard layouts
     */
    @PutMapping("/updateDashboardLayout")
    @Transactional
    public ResponseEntity<List<DashboardLayout>> addDashboardLayouts(@RequestBody List<LayoutDTO> layoutDTOList) {
        List<DashboardLayout> responseLayout = new ArrayList<>();

        for (LayoutDTO tempLayout : layoutDTOList) {
            DashboardLayout currentLayout = dashboardLayoutRepository.findById(tempLayout.moduleId()).orElse(null);
            if (currentLayout != null) {
                currentLayout.setH(tempLayout.h());
                currentLayout.setW(tempLayout.w());
                currentLayout.setY(tempLayout.y());
                currentLayout.setX(tempLayout.x());

                DashboardLayout savedLayout = dashboardLayoutRepository.save(currentLayout);
                responseLayout.add(savedLayout);
            }
        }

        return ResponseEntity.ok(responseLayout);
    }

}
