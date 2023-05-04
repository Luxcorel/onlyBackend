package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is used to send the username and id of a user to the frontend.
 *
 * @param username username of the user
 * @param id       id of the user
 */
public record ProfileDTO(String username, int id) {
}
