package se.onlyfin.onlyfinbackend.model.dashboard_entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DashboardLayout {

    public DashboardLayout (){

    }

    public DashboardLayout(int moduleId, int categoryId){
        this.moduleId = moduleId;
        this.categoryId = categoryId;
        w = 2;
        h = 2;
    }

    @Id
    @Column(name="module_id")
    private int moduleId;

    @Column(name="category_id")
    private int categoryId;

    @Column(name="h")
    private int h;

    @Column(name="w")
    private int w;

    @Column(name="x")
    private int x;

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Column(name="y")
    private int y;

}
