package com.momenta.network;

import com.google.gson.Gson;

public class JsonParser {
    private final Gson gson = new Gson();

    public Quote parseQuote(String json) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Empty JSON response.");
        Quote quote = gson.fromJson(json, Quote.class);
        if (quote == null || quote.getQuote() == null || quote.getQuote().isBlank()) {
            throw new IllegalArgumentException("Invalid quote JSON.");
        }
        return quote;
    }
}
