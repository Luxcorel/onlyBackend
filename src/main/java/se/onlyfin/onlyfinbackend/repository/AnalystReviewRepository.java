package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.AnalystReview;
import se.onlyfin.onlyfinbackend.model.User;

import java.util.Optional;

/**
 * Repository mapping for the analyst review table.
 */
public interface AnalystReviewRepository extends JpaRepository<AnalystReview, Integer> {

    /**
     * Finds an analyst review by the target user and the author username.
     *
     * @param targetUser the target user
     * @param username   the author's username
     * @return an analyst review if found
     */
    Optional<AnalystReview> findByTargetUserAndAuthorUsername(User targetUser, String username);

    /**
     * Deletes an analyst review by the target user and the author username.
     *
     * @param targetUser the target user
     * @param username   the author's username
     */
    void deleteByTargetUserAndAuthorUsername(User targetUser, String username);

    /**
     * Deletes all analyst reviews by the target user.
     *
     * @param targetUsername the target user's username
     */
    void deleteAllByAuthorUsername(String targetUsername);

    /**
     * Deletes all analyst reviews for the target user.
     *
     * @param targetUser the target user
     */
    void deleteAllByTargetUser(User targetUser);

}
