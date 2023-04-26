package se.onlyfin.onlyfinbackend.DTO;

/**
 * This DTO is responsible for holding a profile of a user, and whether the logged-in user is subscribed to that user
 *
 * @param profile    the profile of the analyst.
 * @param subscribed whether the logged-in user is subscribed to that analyst.
 */
public record ProfileWithSubInfoForLoggedInUserDTO(ProfileDTO profile, boolean subscribed) {
}
