package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;

import java.util.ArrayList;
import java.util.List;

public interface DashboardLayoutRepository extends JpaRepository<DashboardLayout, Integer> {

    public List<DashboardLayout> findByCategoryId(Integer categoryId);

}
