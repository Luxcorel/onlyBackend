package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.Content;

public record ModuleChangeDTO(Integer id, Content content, String moduleType) {
}
