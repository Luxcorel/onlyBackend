package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for fetching a category.
 *
 * @param name name of category
 * @param id   id of category
 */
public record CategoryDTO(String name, Integer id) {
}
