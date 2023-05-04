package se.onlyfin.onlyfinbackend.model.dashboard_entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {

    public Category() {

    }

    public Category(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock_id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "category_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleEntity> moduleEntities = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public List<ModuleEntity> getModuleEntities() {
        return moduleEntities; //moduleEntities;
    }

    public void setModuleEntities(List<ModuleEntity> moduleEntities) {
        this.moduleEntities = moduleEntities;
    }

    public int getStock_id() {
        return stock_id.getId();
    }

    public void setStock_id(Stock stock_id) {
        this.stock_id = stock_id;
    }
}
