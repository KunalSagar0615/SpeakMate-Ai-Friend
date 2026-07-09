package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.config.GeminiConfig;
import com.SpeakMate.Ai.friend.config.GroqConfig;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import com.SpeakMate.Ai.friend.service.AiService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class AiServiceImpl implements AiService {

//    private final GeminiConfig geminiConfig;
//
//    @Autowired
//    public AiServiceImpl(GeminiConfig geminiConfig) {
//        this.geminiConfig = geminiConfig;
//    }

    private final GroqConfig groqConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public AiServiceImpl(
            GroqConfig groqConfig,
            RestTemplate restTemplate) {

        this.groqConfig = groqConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateQuestion(
            String topic,
            SessionMode mode, DifficultyLevel difficultyLevel) {

        try {

            String difficultyInstruction;

            switch (difficultyLevel) {

                case BEGINNER ->
                        difficultyInstruction =
                                "Ask beginner level questions using simple concepts and easy language.";

                case INTERMEDIATE ->
                        difficultyInstruction =
                                "Ask intermediate level questions that require practical understanding.";

                case ADVANCED ->
                        difficultyInstruction =
                                "Ask advanced and challenging questions suitable for experienced candidates.";

                default ->
                        difficultyInstruction =
                                "Ask questions appropriate to the user's level.";
            }

            Client client = Client.builder()
                    .apiKey(groqConfig.getApiKey())
                    .build();

            String prompt;

            switch (mode) {

                case INTERVIEW ->
                prompt = """
        You are a technical interviewer.

        Topic: %s

        Difficulty: %s

        Rules:
        - Ask exactly ONE interview question.
        - Focus primarily on theoretical concepts, fundamentals, OOP, Java, Spring Boot, DBMS, SQL, System Design, and practical understanding.
        - Do NOT ask full coding problems.
        - Small coding logic questions are allowed occasionally.
        - Around 80%% theoretical questions and 20%% simple coding logic questions.
        - Questions should match the difficulty level.
        - Return only the question.
        - Do not provide explanations, hints, answers, or markdown.
        """
                        .formatted(topic, difficultyInstruction);

                case FRIEND ->
                        prompt = """
                    You are a friendly AI friend.

                    Topic: %s

                    Start a natural and engaging conversation.

                    Rules:
                    - Ask only ONE friendly question.
                    - Sound casual and human.
                    - Return only the question.
                    """
                                .formatted(topic);

                case ENGLISH_COACH ->
                        prompt = """
                    You are an English speaking coach.

                    Topic: %s

                    Help the user practice spoken English.

                    Rules:
                    - Ask only ONE question.
                    - Encourage the user to answer in English.
                    - Return only the question.
                    """
                                .formatted(topic);

                default ->
                        throw new IllegalStateException(
                                "Unexpected mode: " + mode);
            }

            String groqUrl =
                    "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(groqConfig.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = """
{
  "model": "llama-3.3-70b-versatile",
  "messages": [
    {
      "role": "user",
      "content": %s
    }
  ]
}
""".formatted(
                    new ObjectMapper().writeValueAsString(prompt)
            );

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            groqUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            ObjectMapper mapper = new ObjectMapper();

String aiQuestion = mapper
        .readTree(response.getBody())
        .path("choices")
        .get(0)
        .path("message")
        .path("content")
        .asText();

return aiQuestion;

        } catch (Exception e) {
                throw new RuntimeException(
                        "Groq API Error: " + e.getMessage(), e);

        }
    }

    @Override
    public String generateFeedback(
            String question,
            String answer,
            SessionMode mode) {

        try {

            String prompt = switch (mode) {

                case INTERVIEW ->
                        String.format(
                                """
                                You are an expert technical interviewer.
                
                                Question:
                                %s
                
                                Candidate Answer:
                                %s
                
                                Analyze the answer carefully.
                
                                Rules:
                                - Determine whether the answer is correct, partially correct, or incorrect.
                                - If correct, explain briefly why it is correct.
                                - If partially correct, explain what is correct and what is missing.
                                - If incorrect, explain why it is incorrect and provide the correct answer.
                                - Always refer to the actual question asked.
                                - Do not give generic feedback such as "Good attempt" or "Needs improvement".
                                - Be concise but informative.
                                - Keep feedback within 3-6 sentences.
                                - Return feedback only.
                
                                Feedback:
                                """,
                                question,
                                answer
                        );

                case FRIEND ->
                        String.format(
                                """
                                You are a friendly AI companion.
                
                                Question:
                                %s
                
                                User Response:
                                %s
                
                                Respond naturally like a supportive friend.
                
                                Rules:
                                - Acknowledge the user's response.
                                - React naturally to what the user said.
                                - Do not judge, score, or evaluate.
                                - Keep the response warm and conversational.
                                - Keep it under 3 sentences.
                                - Return feedback only.
                
                                Feedback:
                                """,
                                question,
                                answer
                        );

                case ENGLISH_COACH ->
                        String.format(
                                """
                                You are an English speaking coach.
                
                                User Question:
                                %s
                
                                User Answer:
                                %s
                
                                Analyze the answer.
                
                                Rules:
                                - If grammar mistakes exist, show the corrected sentence.
                                - Explain grammar mistakes briefly.
                                - Suggest better vocabulary only if necessary.
                                - Provide a more natural English version.
                                - If the answer is already good, say so.
                                - Be encouraging and concise.
                                - Do not ask a new question.
                                - Return feedback only.
                
                                Format:
                
                                Corrected Sentence:
                                ...
                
                                Grammar Feedback:
                                ...
                
                                Natural Version:
                                ...
                                """,
                                question,
                                answer
                        );

                default ->
                        throw new IllegalStateException(
                                "Unexpected mode: " + mode);
            };

            String groqUrl =
                    "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(groqConfig.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = """
                {
                  "model": "llama-3.3-70b-versatile",
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ]
                }
                """.formatted(
                    new ObjectMapper().writeValueAsString(prompt)
            );

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            groqUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readTree(response.getBody()).path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq Feedback Error: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public String generateNextQuestion(
            String topic,
            String previousQuestion,
            String userAnswer,
            SessionMode mode,
            DifficultyLevel difficultyLevel) {

        try {

            String difficultyInstruction = switch (difficultyLevel) {

                case BEGINNER ->
                        "Ask beginner-level follow-up questions using simple concepts and easy language suitable for freshers and students.";

                case INTERMEDIATE ->
                        "Ask intermediate-level follow-up questions that require practical understanding.";

                case ADVANCED ->
                        "Ask advanced and challenging follow-up questions suitable for experienced candidates.";

                default ->
                        "Ask questions appropriate to the user's level.";
            };

            String prompt = switch (mode) {

                case INTERVIEW ->
                        String.format(
                                "You are an interviewer. Topic: %s. Previous Question: %s. User Answer: %s. %s Ask exactly ONE relevant follow-up interview question. Return only the question. Do not provide explanations. Do not ask multiple questions.",
                                topic,
                                previousQuestion,
                                userAnswer,
                                difficultyInstruction
                        );

                case FRIEND ->
                        String.format(
                                "You are a friendly AI friend. Topic: %s. Previous Question: %s. User Answer: %s. Continue the conversation naturally. Ask exactly ONE friendly follow-up question. Return only the question. Do not provide explanations. Do not ask multiple questions.",
                                topic,
                                previousQuestion,
                                userAnswer
                        );

                case ENGLISH_COACH ->
                        String.format(
                                "You are an English speaking coach. Topic: %s. Previous Question: %s. User Answer: %s. Ask exactly ONE simple follow-up question. Improve speaking confidence, use easy English, keep the question under 20 words, return only the question, and do not ask multiple questions.",
                                topic,
                                previousQuestion,
                                userAnswer
                        );

                default ->
                        throw new IllegalStateException(
                                "Unexpected mode: " + mode);
            };

            String groqUrl =
                    "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(groqConfig.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = """
                {
                  "model": "llama-3.3-70b-versatile",
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ]
                }
                """.formatted(
                    new ObjectMapper().writeValueAsString(prompt)
            );

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            groqUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            ObjectMapper mapper = new ObjectMapper();

            return mapper
                    .readTree(response.getBody())
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq API Error: " + e.getMessage(),
                    e
            );
        }
    }

//
@Override
public SessionReportDto generateSessionReport(
        String conversationHistory,
        SessionMode mode) {

    try {

        String prompt = switch (mode) {

            case INTERVIEW ->
                    String.format(
                            """
                            You are an expert technical interviewer.

                            Analyze the following interview conversation.

                            %s

                            Return ONLY valid JSON in this exact format:

                            {
                              "overallEvaluation":"...",
                              "strengths":"...",
                              "areasOfImprovement":"...",
                              "recommendations":"..."
                            }

                            Rules:
                            - Return valid JSON only.
                            - Do not use markdown.
                            - Do not use code blocks.
                            - Do not return arrays.
                            - strengths must be a single string.
                            - areasOfImprovement must be a single string.
                            - recommendations must be a single string.
                            - No extra text before or after JSON.
                            """,
                            conversationHistory
                    );

            case FRIEND ->
                    String.format(
                            """
                            You are a conversation analyst.

                            Analyze the following conversation.

                            %s

                            Return ONLY valid JSON in this exact format:

                            {
                              "overallEvaluation":"...",
                              "strengths":"...",
                              "areasOfImprovement":"...",
                              "recommendations":"..."
                            }

                            Rules:
                            - Return valid JSON only.
                            - Do not use markdown.
                            - Do not use code blocks.
                            - Do not return arrays.
                            - strengths must be a single string.
                            - areasOfImprovement must be a single string.
                            - recommendations must be a single string.
                            - No extra text before or after JSON.
                            """,
                            conversationHistory
                    );

            case ENGLISH_COACH ->
                    String.format(
                            """
                            You are an English communication evaluator.

                            Analyze the following conversation.

                            %s

                            Return ONLY valid JSON in this exact format:

                            {
                              "overallEvaluation":"...",
                              "strengths":"...",
                              "areasOfImprovement":"...",
                              "recommendations":"..."
                            }

                            Rules:
                            - Return valid JSON only.
                            - Do not use markdown.
                            - Do not use code blocks.
                            - Do not return arrays.
                            - strengths must be a single string.
                            - areasOfImprovement must be a single string.
                            - recommendations must be a single string.
                            - No extra text before or after JSON.
                            """,
                            conversationHistory
                    );

            default ->
                    throw new IllegalStateException(
                            "Unexpected mode: " + mode);
        };

        String groqUrl =
                "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
            {
              "model": "llama-3.3-70b-versatile",
              "messages": [
                {
                  "role": "user",
                  "content": %s
                }
              ]
            }
            """.formatted(
                new ObjectMapper().writeValueAsString(prompt)
        );

        HttpEntity<String> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        groqUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        ObjectMapper objectMapper = new ObjectMapper();

        String report = objectMapper
                .readTree(response.getBody())
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        System.out.println("========== GROQ REPORT ==========");
        System.out.println(report);
        System.out.println("================================");

        SessionReportDto dto =
                objectMapper.readValue(
                        report,
                        SessionReportDto.class
                );

        System.out.println("========== PARSED DTO ==========");
        System.out.println(dto);
        System.out.println("================================");

        return dto;

    } catch (Exception e) {

        e.printStackTrace();

        return new SessionReportDto(
                null,
                "Unable to generate report.",
                "N/A",
                "N/A",
                "N/A"
        );
    }
}
}