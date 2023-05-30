package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.FeedCard;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository mapping for the feed card table.
 */
public interface FeedCardRepository extends JpaRepository<FeedCard, Integer> {
    /**
     * Find all feed cards for a given analyst.
     *
     * @param name The name of the analyst
     * @return A list of feed cards for the given analyst
     */
    List<FeedCard> findByAnalystUsername(String name);

    /**
     * Find all feed cards for a given analyst after a given date.
     *
     * @param username   The name of the analyst
     * @param cutoffDate The date to start searching from
     * @return A list of feed cards for the given analyst after the given date
     */
    List<FeedCard> findByAnalystUsernameAndPostDateAfterOrderByPostDateDesc(String username, Instant cutoffDate);

    /**
     * @param username The name of the analyst
     * @return A list of feed cards for the given analyst ordered by descending post-date
     */
    List<FeedCard> findByAnalystUsernameOrderByPostDateDesc(String username);

    /**
     * @param analystUsername The name of the analyst
     * @param pageable        The page to return
     * @return A page of feed cards for the given analyst ordered by descending post-date
     */
    Page<FeedCard> findByAnalystUsernameOrderByPostDateDesc(String analystUsername, Pageable pageable);

    /**
     * Finds the latest feed card for a target analyst.
     *
     * @param username name of the target analyst
     * @return the latest feed card for the target analyst
     */
    Optional<FeedCard> findFirstByAnalystUsernameOrderByPostDateDesc(String username);

    /**
     * Finds the most recently updated feed card for a target analyst.
     *
     * @param username name of the target analyst
     * @return the most recently updated feed card for the target analyst
     */
    Optional<FeedCard> findFirstByAnalystUsernameOrderByUpdatedDateDesc(String username);

}
