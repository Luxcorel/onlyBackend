package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;

/**
 * Repository mapping for the dashboard table.
 */
public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {
}
