package com.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

public class GeocodingService {
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingService(String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getCityName(double latitude, double longitude) {
        String url = String.format("http://api.openweathermap.org/geo/1.0/reverse?lat=%s&lon=%s&limit=1&appid=%s",
                latitude, longitude, apiKey);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                System.err.println("Error: " + responseEntity.getStatusCode());
                return "";
            }

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0).get("name").asText(); // Возвращаем имя города
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ""; // Если не удалось получить имя города
    }
}