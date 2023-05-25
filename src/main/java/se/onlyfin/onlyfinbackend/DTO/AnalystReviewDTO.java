package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for fetching an analyst review.
 *
 * @param author     the post's author's username
 * @param reviewText the review text
 */
public record AnalystReviewDTO(String author, String reviewText) {
}
