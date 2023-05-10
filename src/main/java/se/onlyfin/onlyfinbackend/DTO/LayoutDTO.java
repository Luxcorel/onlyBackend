package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.DashboardLayout;

import java.util.List;

public record LayoutDTO(Integer categoryId, Integer moduleId, Integer x, Integer y, Integer h, Integer w) {
}
