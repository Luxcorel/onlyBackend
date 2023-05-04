package se.onlyfin.onlyfinbackend.DTO;

import jakarta.validation.constraints.Email;

/**
 * This DTO is responsible for containing the user registration forms data.
 *
 * @param email    the email of the user
 * @param username the username of the user
 * @param password the password of the user
 */
public record UserDTO(@Email String email, String username, String password) {
}
