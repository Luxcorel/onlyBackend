package se.onlyfin.onlyfinbackend.DTO;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO for a card in the feed.
 *
 * @param posterOfContent the user who posted the content
 * @param stock           the stock that the content is about
 * @param category        the category that the content is about
 * @param content         the content of the card
 * @param postDate        the date that the content was posted
 * @param updatedAt       the date that the content was last updated
 */
public record FeedCardDTO(ProfileDTO posterOfContent, StockDTO stock, CategoryDTO category, JsonNode content,
                          String postDate, String updatedAt) {
}
