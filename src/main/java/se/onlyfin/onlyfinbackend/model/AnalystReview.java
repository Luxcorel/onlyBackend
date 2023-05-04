package se.onlyfin.onlyfinbackend.model;

import jakarta.persistence.*;

/**
 * This class represents a review of an analyst.
 */
@Entity
@Table(name = "analyst_review")
public class AnalystReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "author_username")
    private String authorUsername;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(name = "review_text")
    private String reviewText;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
