package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;

import java.util.List;

/**
 * Repository mapping for the dashboard layout table.
 */
public interface DashboardLayoutRepository extends JpaRepository<DashboardLayout, Integer> {

    /**
     * Finds all dashboard layouts by category id.
     *
     * @param categoryId target category id
     * @return dashboard layouts
     */
    List<DashboardLayout> findByCategoryId(Integer categoryId);

}
