package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for containing information about a stock reference.
 *
 * @param stockRefId  id of stock reference
 * @param dashboardId id of the dashboard that contains the stock reference
 */
public record StockRefDTO(Integer stockRefId, Integer dashboardId) {
}
