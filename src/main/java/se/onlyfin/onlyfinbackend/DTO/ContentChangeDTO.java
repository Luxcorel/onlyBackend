package se.onlyfin.onlyfinbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ContentChangeDTO {
    @JsonProperty("id")
    private int id;

    @JsonProperty("content")
    private JsonNode content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setName(JsonNode content) {
        this.content = content;
    }
}
