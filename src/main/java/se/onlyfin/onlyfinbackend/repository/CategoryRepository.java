package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Category;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;

/**
 * Repository mapping for the category table.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT d.id FROM Category c JOIN c.stock_id s JOIN s.dashboard_id d WHERE c.id = :categoryID")
    Integer findDashboardFromCategoryId(Integer categoryID);
}
