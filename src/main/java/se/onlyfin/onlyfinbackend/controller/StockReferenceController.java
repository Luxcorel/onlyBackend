package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;
import se.onlyfin.onlyfinbackend.repository.StockRefRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/stonks")
@RestController
@CrossOrigin(origins = "https://onlybackend-production.up.railway.app", allowCredentials = "true")
public class StockReferenceController {
    private final StockRefRepository stockRefRepository;

    public StockReferenceController(StockRefRepository stockRefRepository) {
        this.stockRefRepository = stockRefRepository;
    }

    @GetMapping("/search-stonk-by-name")
    public ResponseEntity<List<StockRef>> fetchStocksUsingName(@RequestParam String search) {
        if (search.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<StockRef> searchResults = new ArrayList<>(stockRefRepository.findTop7StockRefsByNameIgnoreCaseStartingWith(search));
        if (searchResults.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(searchResults);
    }

    @GetMapping("/search-stonk-by-ticker")
    public ResponseEntity<List<StockRef>> fetchStocksUsingTicker(@RequestParam String search) {
        if (search.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<StockRef> searchResults = new ArrayList<>(stockRefRepository.findTop7StockRefsByTickerIgnoreCaseStartingWith(search));
        if (searchResults.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(searchResults);
    }

    public List<StockRef> fetchStockReferencesUsingName(String stockName) {
        return new ArrayList<>(stockRefRepository.findTop7StockRefsByNameIgnoreCaseStartingWith(stockName));
    }

    public Optional<StockRef> fetchStockRefByName(String stockName) {
        return stockRefRepository.findStockRefByName(stockName);
    }

}
