package com.SpeakMate.Ai.friend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqConfig {

    @Value("${groq.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}