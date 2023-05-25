package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;

/**
 * Repository mapping for the stock table.
 */
public interface StockRepository extends JpaRepository<Stock, Integer> {
}
