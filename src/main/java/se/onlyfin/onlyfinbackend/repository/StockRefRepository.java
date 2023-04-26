package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;

import java.util.List;
import java.util.Optional;

public interface StockRefRepository extends JpaRepository<StockRef, Integer> {

    List<StockRef> findTop7StockRefsByNameIgnoreCaseStartingWith(String name);

    List<StockRef> findTop7StockRefsByTickerIgnoreCaseStartingWith(String search);

    Optional<StockRef> findStockRefByName(String stockName);
}
