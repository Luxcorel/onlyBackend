package se.onlyfin.onlyfinbackend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO used for updating a target category's name.
 *
 * @param name new name
 * @param id   target category id
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public record CategoryNameChangeDTO(String name, Integer id) {
}
