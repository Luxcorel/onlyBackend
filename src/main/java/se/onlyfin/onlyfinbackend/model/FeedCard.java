package se.onlyfin.onlyfinbackend.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * This class represents a feed card in the database.
 */
@Entity
@Table(name = "feed_card")
public class FeedCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "analyst_username")
    private String analystUsername;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "post_date")
    private Instant postDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "content")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode content;

    @Column(name = "module_type")
    private String moduleType;

    @Column(name = "category_id")
    private Integer categoryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAnalystUsername() {
        return analystUsername;
    }

    public void setAnalystUsername(String analystUsername) {
        this.analystUsername = analystUsername;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Instant getPostDate() {
        return postDate;
    }

    public void setPostDate(Instant postDate) {
        this.postDate = postDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
