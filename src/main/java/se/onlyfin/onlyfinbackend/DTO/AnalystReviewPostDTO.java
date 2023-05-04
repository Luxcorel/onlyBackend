package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is responsible for holding data for the posting an analyst review
 *
 * @param targetUsername The username of the analyst that the review is for.
 * @param reviewText     The text of the review.
 */
public record AnalystReviewPostDTO(String targetUsername, String reviewText) {
}
