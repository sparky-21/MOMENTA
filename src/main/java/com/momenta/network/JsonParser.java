package com.momenta.network;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

    // Quote only exposes getters, no setters — the same shape Gson was
    // happy to populate via reflection. Telling Jackson to read fields
    // directly (regardless of getter/setter visibility) keeps that model
    // class unchanged instead of having to add setters just for Jackson.
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Quote parseQuote(String json) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Empty JSON response.");

        Quote quote;
        try {
            quote = mapper.readValue(json, Quote.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid quote JSON.", e);
        }

        if (quote == null || quote.getQuote() == null || quote.getQuote().isBlank()) {
            throw new IllegalArgumentException("Invalid quote JSON.");
        }
        return quote;
    }
}
