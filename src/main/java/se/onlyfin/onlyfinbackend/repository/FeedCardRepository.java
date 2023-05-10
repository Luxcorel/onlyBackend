package se.onlyfin.onlyfinbackend.repository;

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

    Optional<FeedCard> findFirstByAnalystUsernameOrderByPostDateDesc(String username);

    Optional<FeedCard> findFirstByAnalystUsernameOrderByUpdatedDateDesc(String username);

}
