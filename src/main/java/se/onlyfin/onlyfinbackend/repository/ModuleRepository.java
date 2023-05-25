package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.ModuleEntity;

/**
 * Repository mapping for the module table.
 */
public interface ModuleRepository extends JpaRepository<ModuleEntity, Integer> {

    @Query("SELECT s.dashboard_id FROM ModuleEntity m JOIN m.category_id c JOIN c.stock_id s WHERE m.id = :moduleId")
    Dashboard findDashboardByModuleId(Integer moduleId);
}
