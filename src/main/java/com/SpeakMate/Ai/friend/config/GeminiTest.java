package com.SpeakMate.Ai.friend.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeminiTest implements CommandLineRunner {

    @Autowired
    private GeminiConfig geminiConfig;

    @Override
    public void run(String... args) throws Exception {

        Client client = Client.builder()
                .apiKey(geminiConfig.getApiKey())
                .build();

        System.out.println("Gemini Client Created Successfully");
    }
}
