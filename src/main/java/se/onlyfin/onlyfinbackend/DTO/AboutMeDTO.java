package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for fetching an about me page.
 *
 * @param aboutMe    the about me text
 * @param subscribed if the logged-in user is subscribed to the target analyst
 */
public record AboutMeDTO(String aboutMe, Boolean subscribed) {
}
