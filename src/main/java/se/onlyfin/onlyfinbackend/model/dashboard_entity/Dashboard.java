package se.onlyfin.onlyfinbackend.model.dashboard_entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the dashboard table in the database.
 */
@Entity
@Table(name = "dashboard")
public class Dashboard {

    public Dashboard() {
    }

    public Dashboard(int id) {
        this.id = id;
    }

    @Id
    private int id;

    @OneToMany(mappedBy = "dashboard_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    @Override
    public String toString() {
        return id + "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
