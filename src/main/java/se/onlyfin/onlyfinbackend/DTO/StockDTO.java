package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for holding data about a stock from a dashboard.
 *
 * @param name name of the stock
 * @param id   id of the stock
 */
public record StockDTO(String name, Integer id) {
}
