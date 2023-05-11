package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.repository.CrudRepository;
import se.onlyfin.onlyfinbackend.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository mapping for the user table.
 */
public interface UserRepository extends CrudRepository<User, Integer> {
    /**
     * Find any user by email
     *
     * @param username the email of the user
     * @return the user if found
     */
    Optional<User> findByEmail(String username);

    /**
     * Find any user by username
     *
     * @param username the username of the user
     * @return the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if any user exists by username
     *
     * @param username the username of the user
     * @return true if the user exists
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Check if any user exists by email
     *
     * @param email the email of the user
     * @return true if the user exists
     */
    boolean existsByEmail(String email);

    /**
     * Delete any user by email
     *
     * @param email the email of the user
     * @return true if the user was deleted
     */
    boolean deleteByEmail(String email);

    /**
     * Find all users that are analysts
     *
     * @return all users that are analysts
     */
    List<User> findByisAnalystIsTrue();

    /**
     * Find analyst by username
     *
     * @param username the username of the analyst
     * @return the analyst if found
     */
    Optional<User> findByisAnalystIsTrueAndUsernameEquals(String username);

    /**
     * Find all analysts that start with the given search term
     *
     * @param search the search term
     * @return all analysts that start with the given search term
     */
    List<User> findByisAnalystIsTrueAndUsernameStartsWith(String search);

    /**
     * Find the most recent 7 analysts whose username starts with the given search term
     *
     * @param search the search term
     * @return up to 7 analysts that start with the given search term
     */
    List<User> findTop7ByisAnalystIsTrueAndUsernameIgnoreCaseStartsWith(String search);
}
