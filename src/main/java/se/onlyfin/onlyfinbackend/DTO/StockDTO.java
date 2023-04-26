package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is responsible for holding data about a stock from the dashboard
 *
 * @param name name of the stock
 * @param id   id of the stock
 */
public record StockDTO(String name, Integer id) {
}
