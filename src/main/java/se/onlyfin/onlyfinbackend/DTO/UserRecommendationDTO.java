package se.onlyfin.onlyfinbackend.DTO;

import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;

import java.util.Objects;

/**
 * DTO used for sending a recommendation of an analyst.
 * It contains the common stock that were the reason for the recommendation and the profile of the suggested analyst.
 * Overridden equal & hashcode so that only the profile object is subject to comparison.
 * This is the case as recommendations of the same user should be interpreted as the same.
 *
 * @param stock      stock that analyst was recommended for covering
 * @param profileDTO profile of the recommended analyst
 */
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
