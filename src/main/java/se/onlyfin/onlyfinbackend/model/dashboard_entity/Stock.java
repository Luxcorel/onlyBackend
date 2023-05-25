package se.onlyfin.onlyfinbackend.model.dashboard_entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the stock table in the database.
 */
@Entity
@Table(name = "stock")
public class Stock {

    public Stock() {
    }

    public Stock(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_ref_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StockRef stock_ref_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id")
    private Dashboard dashboard_id;

    @OneToMany(mappedBy = "stock_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    public int getDashboard_id() {
        return dashboard_id.getId();
    }

    public int getId() {
        return id;
    }

    public StockRef getStock_ref_id() {
        return stock_ref_id;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStock_ref_id(StockRef stock_ref_id) {
        this.stock_ref_id = stock_ref_id;
    }

    public void setDashboard_id(Dashboard dashboard_id) {
        this.dashboard_id = dashboard_id;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getName() {
        return stock_ref_id.getName();
    }

    public int getStockRefId() {
        return stock_ref_id.getId();
    }

}

