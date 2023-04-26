package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is responsible for holding data for the about me page.
 *
 * @param aboutMe    the about me text
 * @param subscribed if the logged-in user is subscribed to the target analyst
 */
public record AboutMeDTO(String aboutMe, Boolean subscribed) {
}
