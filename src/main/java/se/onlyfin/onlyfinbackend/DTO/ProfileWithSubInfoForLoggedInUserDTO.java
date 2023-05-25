package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for fetching a profile of a user, and whether the logged-in user is subscribed to that user.
 *
 * @param profile    the profile of the analyst.
 * @param subscribed whether the logged-in user is subscribed to that analyst.
 */
public record ProfileWithSubInfoForLoggedInUserDTO(ProfileDTO profile, boolean subscribed) {
}
