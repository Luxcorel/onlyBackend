package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is used to update the password of a user.
 *
 * @param oldPassword The old password
 * @param newPassword The new password
 */
public record PasswordUpdateDTO(String oldPassword, String newPassword) {
}
