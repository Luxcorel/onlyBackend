package se.onlyfin.onlyfinbackend.DTO;

import java.util.Objects;

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
