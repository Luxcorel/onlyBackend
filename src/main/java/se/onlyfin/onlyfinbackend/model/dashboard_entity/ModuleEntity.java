package se.onlyfin.onlyfinbackend.model.dashboard_entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "module")
public class ModuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category_id;

    @Column(name = "content")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode content;

    @Column(name = "module_type")
    private String module_type;

    @Column(name = "post_date", updatable = false, insertable = false)
    private Instant postDate;

    @Column(name = "updated_date", updatable = false, insertable = false)
    private Instant updatedDate;

    @Column(name = "feed_card_id", insertable = false, updatable = false)
    private String feedCardId;

    public int getId() {
        return id;
    }

    public int getCategory_id() {
        return category_id.getId();
    }

    public JsonNode getContent() {
        return content;
    }

    public String getModule_type() {
        return module_type;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public void setModule_type(JsonNode module_type) {
        this.module_type = module_type.asText();
    }

    public void setCategory_id(JsonNode categoryId) {
        try {
            this.category_id = new Category(Integer.parseInt(categoryId.asText()));
        } catch (Exception e) {
            System.out.println(e);
        }
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

    @Override
    public String toString() {
        return "ModuleEntity{" +
                "category_id=" + category_id.getId() +
                ", content=" + content +
                ", module_type='" + module_type + '\'' +
                '}';
    }
}
