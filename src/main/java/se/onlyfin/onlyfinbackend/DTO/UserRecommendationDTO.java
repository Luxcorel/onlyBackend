package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;

import java.util.Objects;

public record UserRecommendationDTO(StockRef stock, ProfileDTO profileDTO) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecommendationDTO that = (UserRecommendationDTO) o;
        return Objects.equals(profileDTO, that.profileDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileDTO);
    }
}
