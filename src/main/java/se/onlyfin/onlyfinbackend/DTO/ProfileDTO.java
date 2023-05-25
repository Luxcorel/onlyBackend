package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used to send the username and id of a user.
 *
 * @param username username of the user
 * @param id       id of the user
 */
public record ProfileDTO(String username, int id) {
}
