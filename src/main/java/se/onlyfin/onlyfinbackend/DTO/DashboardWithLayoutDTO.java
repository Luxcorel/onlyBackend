package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;

import java.util.List;

public record DashboardWithLayoutDTO (Dashboard dashboard, List<DashboardLayout> dashboardLayout) {
}
