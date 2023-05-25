package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;

import java.util.List;

/**
 * DTO used for fetching a dashboard and its module layouts.
 *
 * @param dashboard       the dashboard
 * @param dashboardLayout the dashboard's layout
 */
public record DashboardWithLayoutDTO(Dashboard dashboard, List<DashboardLayout> dashboardLayout) {
}
