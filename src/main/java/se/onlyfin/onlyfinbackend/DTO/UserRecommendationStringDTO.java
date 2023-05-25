package se.onlyfin.onlyfinbackend.DTO;

import java.util.Objects;

/**
 * Variation of the {@link UserRecommendationDTO} with just the stock name instead of a complete stock object.
 *
 * @param stock      name of stock that the suggested analyst was recommended for covering
 * @param profileDTO profile of suggested analyst
 */
public record UserRecommendationStringDTO(String stock, ProfileDTO profileDTO) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecommendationStringDTO that = (UserRecommendationStringDTO) o;
        return Objects.equals(profileDTO, that.profileDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileDTO);
    }
}
