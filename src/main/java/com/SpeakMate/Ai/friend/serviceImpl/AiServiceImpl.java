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
                                You are an interviewer.
                
                                Question:
                                %s
                
                                Candidate Answer:
                                %s
                
                                Evaluate the answer exactly like a real interviewer.
                
                                Rules:
                                - First determine the verdict: Correct, Partially Correct, or Incorrect.
                                - Mention specifically what the candidate answered correctly.
                                - Mention specific missing or incorrect points.
                                - Provide the correct explanation when needed.
                                - Focus only on concepts related to the question.
                                - Do not praise unnecessarily.
                                - Do not use generic phrases such as "Good attempt", "Needs improvement", or "Could be more comprehensive".
                                - If the answer is completely wrong, teach the concept briefly.
                                - Keep feedback concise (80-120 words).
                                - Do not ask a new question.
                                - Return feedback only.
                
                                Format:
                
                                Verdict: <Correct/Partially Correct/Incorrect>
                
                                What You Got Right:
                                ...
                
                                What Was Missing:
                                ...
                
                                Correct Explanation:
                                ...
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
                
                                Analyze the answer carefully.
                
                                Rules:
                                - Correct grammar mistakes if present.
                                - Explain mistakes briefly.
                                - Suggest better vocabulary only when necessary.
                                - If the answer is already natural and correct, clearly say so.
                                - Do not invent mistakes.
                                - Do not ask another question.
                                - Keep feedback concise.
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

                case FRIEND ->
                        String.format(
                                """
                                You are a friendly AI companion.
                
                                Question:
                                %s
                
                                User Response:
                                %s
                
                                Rules:
                                - Respond naturally to what the user said.
                                - Continue the conversation like a real friend.
                                - Show empathy when appropriate.
                                - Do not evaluate, score, or correct the answer.
                                - Avoid repetitive phrases.
                                - Keep the response conversational and engaging.
                                - Maximum 2-3 sentences.
                                - Return only the response.
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
            String previousQuestions,
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
                                "You are an expert technical interviewer. Topic: %s. Previously Asked Questions: %s. User's Latest Answer: %s. %s Generate exactly ONE interview question. The question MUST remain strictly within the topic '%s'. Never repeat, rephrase, or ask about concepts already covered in the previously asked questions. Explore a different subtopic, feature, comparison, best practice, use case, real-world scenario, advantage/disadvantage, or problem-solving aspect of the same topic. Prefer theoretical and conceptual questions. Occasionally ask small logic-based coding questions but avoid large coding problems. Maintain a natural interview progression from basic to advanced according to the difficulty level. Return ONLY the question text. Do not provide explanations, answers, feedback, numbering, or multiple questions.",
                                topic,
                                previousQuestions,
                                userAnswer,
                                difficultyInstruction,
                                topic
                        );

                case FRIEND ->
                        String.format(
                                "You are a friendly AI friend. Topic: %s. Previously Asked Questions: %s. User Answer: %s. Continue the conversation naturally and engagingly. Never repeat or rephrase any previously asked question. Avoid asking about the same aspect repeatedly. Explore different aspects of the topic while keeping the conversation friendly, human-like, and enjoyable. Ask exactly ONE follow-up question. Return ONLY the question text. Do not provide explanations, comments, feedback, numbering, or multiple questions.",
                                topic,
                                previousQuestions,
                                userAnswer
                        );

                case ENGLISH_COACH ->
                        String.format(
                                "You are an English speaking coach. Topic: %s. Previously Asked Questions: %s. User Answer: %s. Ask exactly ONE simple follow-up question. Never repeat or rephrase any previously asked question. Explore different aspects of the topic to improve vocabulary, fluency, confidence, and sentence formation. Use easy and natural English. Keep the question under 20 words. Return ONLY the question text. Do not provide explanations, corrections, feedback, numbering, or multiple questions.",
                                topic,
                                previousQuestions,
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

    @Override
    public String generateSuggestedAnswer(
            String question,
            SessionMode mode,
            DifficultyLevel difficultyLevel) {

        String prompt = String.format(
                """
                You are an expert tutor.
    
                Question:
                %s
    
                Mode:
                %s
    
                Difficulty:
                %s
    
                Generate an ideal answer.
    
                Rules:
                - Answer the question directly.
                - Keep the answer concise.
                - Use simple language.
                - 3 to 8 sentences.
                - Return only the answer.
                """,
                question,
                mode,
                difficultyLevel
        );

        return callGroq(prompt);
    }

    private String callGroq(String prompt) {

        try {

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
}