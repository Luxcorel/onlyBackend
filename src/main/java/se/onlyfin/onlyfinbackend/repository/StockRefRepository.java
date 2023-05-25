package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;

import java.util.List;
import java.util.Optional;

/**
 * Repository mapping for the stock reference table.
 */
public interface StockRefRepository extends JpaRepository<StockRef, Integer> {

    /**
     * Returns a list of max 7 StockRef's starting with the target stock name
     *
     * @param name name of the target stock
     * @return list of max 7 StocksRefs
     */
    List<StockRef> findTop7StockRefsByNameIgnoreCaseStartingWith(String name);

    /**
     * Returns a list of max 7 StockRef's starting with the target ticker name
     *
     * @param search name of the target ticker
     * @return list of max 7 StocksRefs
     */
    List<StockRef> findTop7StockRefsByTickerIgnoreCaseStartingWith(String search);

    /**
     * Returns a StockRef using its exact name
     *
     * @param stockName exact name of target StockRef
     * @return StockRef if it exists
     */
    Optional<StockRef> findStockRefByName(String stockName);
}
