package com.SpeakMate.Ai.friend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;

@org.springframework.context.annotation.Configuration
public class BrevoConfig {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi() {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        ApiKeyAuth apiKeyAuth =
                (ApiKeyAuth) defaultClient.getAuthentication("api-key");

        apiKeyAuth.setApiKey(apiKey);

        return new TransactionalEmailsApi(defaultClient);
    }
}