package se.onlyfin.onlyfinbackend.DTO;

/**
 * DTO used for fetching/updating a module's dashboard layout properties.
 *
 * @param categoryId target category id
 * @param moduleId   target module id
 * @param x          x coordinate
 * @param y          y coordinate
 * @param h          height of module
 * @param w          width of module
 */
public record LayoutDTO(Integer categoryId, Integer moduleId, Integer x, Integer y, Integer h, Integer w) {
}
