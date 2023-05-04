package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is meant to contain information about an analyst review
 *
 * @param author     the post's author's username
 * @param reviewText the review text
 */
public record AnalystReviewDTO(String author, String reviewText) {
}
