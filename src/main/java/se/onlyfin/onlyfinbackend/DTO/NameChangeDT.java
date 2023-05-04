package se.onlyfin.onlyfinbackend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public record NameChangeDT(String name, Integer id) {
}
