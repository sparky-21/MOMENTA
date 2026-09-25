package com.momenta.network;

public class InsightService {
    private final NetworkService networkService = new NetworkService();
    private final JsonParser jsonParser = new JsonParser();

    public Quote getDailyInsight() throws Exception {
        return jsonParser.parseQuote(networkService.getRandomQuote());
    }
}
