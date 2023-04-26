package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is responsible for containing the user registration forms data.
 *
 * @param email    the email of the user
 * @param username the username of the user
 * @param password the password of the user
 */
public record UserDTO(String email, String username, String password) {

}
