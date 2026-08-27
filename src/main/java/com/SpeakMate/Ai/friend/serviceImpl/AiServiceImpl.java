package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.config.GroqConfig;
import com.SpeakMate.Ai.friend.dto.AiAnswerEvaluationDto;
import com.SpeakMate.Ai.friend.dto.AiQuestionExtractionDto;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import com.SpeakMate.Ai.friend.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
            SessionMode mode,
            DifficultyLevel difficultyLevel) {

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

            String prompt;

            switch (mode) {

                case INTERVIEW ->
                        prompt = """
        You are a technical interviewer.

        Topic: %s

        Difficulty: %s

        Rules:
        - Ask exactly ONE interview question.
        - The question must be strictly relevant to the given topic "%s" — do not assume any specific field, language, or domain unless the topic itself specifies it.
        - Focus primarily on core concepts, fundamentals, and practical understanding within that topic.
        - Do NOT ask full coding/practical problems that require writing something down.
        - Small logic-based or scenario-based questions are allowed occasionally, but only in a form the user can explain verbally.
        - The question must be fully answerable by speaking — never ask the user to write code, write formulas, draw a diagram, or produce written output.
        - Around 80%% conceptual/theoretical questions and 20%% applied/scenario-based questions (explained verbally).
        - Ask the question directly — do NOT add any reflective or empathetic preamble.
        - Keep the question under 30 words, single sentence.
        - Questions should match the difficulty level.
        - Return only the question.
        - Do not provide explanations, hints, answers, or markdown.
        """
                                .formatted(topic, difficultyInstruction, topic);

                case FRIEND ->
                        prompt = """
                    You are a friendly AI friend.

                    Topic: %s

                    Start a natural and engaging conversation.

                    Rules:
                    - Ask only ONE friendly question.
                    - Sound casual and human.
                    - Keep it under 20 words, single sentence, no compound double-clause questions.
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
                    - Keep it under 20 words.
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
  "model": "openai/gpt-oss-120b",
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
                                You are a professional technical interviewer evaluating a candidate's spoken answer.
            
                                Question:
                                %s
            
                                Candidate Answer:
                                %s
            
                                Evaluate the candidate's answer naturally and concisely.
            
                                Rules:
                                - If the candidate explicitly asks to skip or change the topic/question:
                                  reply only with a brief natural acknowledgment such as
                                  "No problem, let's move to a different question."
                                  Do not evaluate the answer.
            
                                - If the candidate indicates confusion or says they did not understand
                                  the question:
                                  reply only with a brief natural acknowledgment such as
                                  "No worries, let me put that more simply."
                                  Do not restate or explain the question.
            
                                - If the candidate says they do not know the answer, asks you to provide
                                  the answer, or gives no meaningful attempt:
                                  briefly explain the correct concept naturally.
            
                                - Otherwise, determine whether the answer is correct, partially correct,
                                  or incorrect.
            
                                - Focus primarily on whether the core concept is correct.
                                - If the answer is correct, briefly confirm what was correct and mention
                                  one useful detail only when it adds value.
                                - If the answer is partially correct, naturally explain what important
                                  concept or detail is missing.
                                - If the answer is incorrect, briefly explain the correct concept.
                                - Do not repeat the full question.
                                - Do not repeat the candidate's entire answer.
                                - Do not use generic filler such as "Good attempt", "Great answer",
                                  "That's a great point", "Needs improvement", or "It's great that".
                                - Do not score the answer.
                                - Do not ask another question.
                                - Keep the response between 15 and 40 words.
                                - The response must sound like natural human interviewer feedback.
                                - Do NOT use labels such as "Verdict:", "Feedback:", "Result:",
                                  "Evaluation:", or similar.
                                - Do NOT use bullet points or markdown.
                                - Return only the natural feedback sentence(s).
            
                                Examples:
            
                                Correct:
                                "Correct. @SpringBootApplication enables auto-configuration and component scanning, making it a convenient entry point for a Spring Boot application."
            
                                Partially correct:
                                "You're on the right track, but you missed that continue skips the current iteration while break terminates the loop completely."
            
                                Incorrect:
                                "That's incorrect. The Java compiler converts source code into bytecode, which can then be executed by the JVM."
            
                                Return only the feedback text.
                                """,
                                question,
                                answer
                        );

                case ENGLISH_COACH ->
                        String.format(
                                """
                                You are a friendly and professional English speaking coach.
            
                                User Question:
                                %s
            
                                User Answer:
                                %s
            
                                Evaluate the user's response naturally.
            
                                Rules:
                                - If the user explicitly asks to skip or change the topic/question:
                                  reply only with a brief natural acknowledgment such as
                                  "Sure, let's try something else."
                                  Do not evaluate the answer.
            
                                - If the user indicates confusion or says they did not understand
                                  the question:
                                  reply only with a brief natural acknowledgment such as
                                  "No worries, let me simplify that."
                                  Do not restate or explain the question.
            
                                - If the user says they do not know, gives no meaningful attempt,
                                  or gives an empty/off-topic response:
                                  provide a short natural example of a suitable answer in English.
            
                                - Otherwise evaluate BOTH:
                                  1. Grammar and sentence structure.
                                  2. Whether the answer actually makes sense and answers the question.
            
                                - If the English is grammatically correct, do not invent corrections.
                                - If there are grammar mistakes, naturally provide the corrected form.
                                - If the answer does not properly answer the question, briefly explain
                                  what needs to be improved.
                                - If both grammar and content are good, simply confirm that naturally.
                                - Do not repeat the full question or answer.
                                - Do not use generic filler such as "Good attempt", "Great job",
                                  "That's a great point", or "It's great that".
                                - Do not ask another question.
                                - Keep the response between 15 and 40 words.
                                - Do NOT use labels such as "Corrected Sentence:", "Feedback:",
                                  "Verdict:", "Evaluation:", or similar.
                                - Do NOT use bullet points or markdown.
                                - Return only natural coaching feedback.
            
                                Examples:
            
                                Good answer:
                                "Your answer is clear and grammatically correct. It directly addresses the question and uses natural English."
            
                                Grammar issue:
                                "Your idea is clear, but a more natural sentence would be: \"I have been learning Java for two years.\""
            
                                Content issue:
                                "Your English is understandable, but the answer does not fully address the question. Try explaining the main purpose more directly."
            
                                Return only the feedback text.
                                """,
                                question,
                                answer
                        );

                case FRIEND ->
                        String.format(
                                """
                                You are a supportive AI friend having a natural conversation.
            
                                Question:
                                %s
            
                                User Response:
                                %s
            
                                React naturally to the user's response.
            
                                Rules:
                                - If the user explicitly asks to skip, change the topic, or move to
                                  something else:
                                  reply only with a brief natural acknowledgment such as
                                  "Sure, let's talk about something else."
                                  Nothing more.
            
                                - If the user says they did not understand the question, seems confused,
                                  or asks you to explain or repeat it:
                                  reply only with a brief natural acknowledgment such as
                                  "No worries, let me simplify that for you."
                                  Do not restate or answer the question.
            
                                - If the user says they do not know or gives no meaningful attempt,
                                  gently explain the concept or answer in a friendly way.
            
                                - Otherwise, react naturally and specifically to what the user said.
                                - Do not evaluate, score, or judge the user.
                                - Do not ask a new question.
                                - Do not end with a question mark.
                                - Do not use generic filler such as "That's a great point",
                                  "That's awesome", "It's great that", or "You seem to be".
                                - Keep the response warm, casual, natural, and human.
                                - Keep the response between 15 and 40 words.
                                - If the response is understandable and mostly grammatically correct,
                                  simply react naturally without rewriting it.
                                - If the response has major grammar problems or broken structure,
                                  silently rewrite it into natural English.
                                - Do not explain grammar rules.
                                - Do not mention words such as "grammar", "incorrect", "wrong", or "error".
                                - Do NOT use labels such as "Feedback:", "Verdict:", "Correction:",
                                  or similar.
                                - Do NOT use bullet points or markdown.
                                - Return only the natural response text.
            
                                Return only the response text.
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
                  "model": "openai/gpt-oss-120b",
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
                                """
                                You are an expert technical interviewer.

                                Topic:
                                %s

                                Previously Asked Questions (latest 20 from the same topic, mode, and difficulty — this list is authoritative; the last item in it IS the question that was just asked):
                                %s

                                Candidate's Latest Answer:
                                %s

                                Difficulty Guidance:
                                %s

                                Your task is to generate the next interview question.

                                Rules:
                                - If the candidate's latest answer explicitly asks to skip or change the topic/question: pick a genuinely new concept or subtopic within "%s" that is unrelated to the recent question thread, and ask that as a fresh question.
                                - If the candidate's latest answer indicates confusion (e.g. "I didn't get your question", "I don't understand", "can you repeat", "explain simply"): take the LAST question from Previously Asked Questions and return a simpler, shorter rewording of that exact same question. Never claim no previous question exists — the list always contains one.
                                - Otherwise, ask EXACTLY ONE interview question.
                                - The question must remain strictly within the topic "%s" — do not assume any specific field, language, or domain unless the topic itself specifies it.
                                - NEVER repeat, rephrase, or slightly modify any previously asked question.
                                - NEVER test the same concept again unless absolutely necessary.
                                - Avoid circling back to the same theme or subtopic across multiple questions in a row — after covering a theme once, move to a genuinely different concept.
                                - Cover the topic progressively from fundamentals to advanced concepts.
                                - Use the candidate's latest answer to guide difficulty: if it showed a weak or incorrect understanding, ask a slightly simpler or clarifying question on a related concept; if it showed strong understanding, escalate to a harder or deeper question.
                                - The question must be fully answerable by speaking — never ask the user to write code, write formulas, draw a diagram, or produce written output.
                                - Around 80%% conceptual/theoretical questions and 20%% applied/scenario-based questions (explained verbally).
                                - Small logic-based or scenario-based questions are allowed occasionally, but only in a form the user can explain verbally.
                                - Ask the question directly — do NOT add a reflective/empathetic preamble commenting on the previous answer.
                                - Keep the question under 30 words, single sentence, no compound double-clause questions.
                                - Questions should match the difficulty level.
                                - Do NOT ask multiple questions.
                                - Do NOT provide explanations, hints, answers, numbering, or markdown.
                                - Return ONLY the question text.
                                """,
                                topic,
                                previousQuestions,
                                userAnswer,
                                difficultyInstruction,
                                topic,
                                topic
                        );

                case FRIEND ->
                        String.format(
                                """
                                You are a friendly AI friend having a natural conversation.

                                Topic:
                                %s

                                Previously Asked Questions (this list is authoritative; the last item in it IS the question that was just asked):
                                %s

                                User's Last Answer:
                                %s

                                Your task is to continue the conversation naturally.

                                Rules:
                                - If the user's last answer explicitly asks to skip, change the topic, or talk about something else: pick a genuinely new, unrelated topic direction and ask a fresh question about it.
                                - If the user's last answer indicates confusion: take the LAST question from Previously Asked Questions and return a simpler, shorter rewording of that exact same question.
                                - Otherwise, ask EXACTLY ONE follow-up question.
                                - NEVER repeat or rephrase any previously asked question.
                                - Avoid circling back to the same theme repeatedly.
                                - Vary the question's structure and phrasing.
                                - Use the user's last answer to pick a natural next direction.
                                - Ask the question directly.
                                - Keep the question under 20 words, single sentence.
                                - Sound casual, warm, and human.
                                - Return ONLY the question text.
                                - Do not provide explanations, comments, feedback, numbering, or multiple questions.
                                """,
                                topic,
                                previousQuestions,
                                userAnswer
                        );

                case ENGLISH_COACH ->
                        String.format(
                                """
                                You are an English speaking coach.

                                Topic:
                                %s

                                Previously Asked Questions (this list is authoritative; the last item in it IS the question that was just asked):
                                %s

                                User's Last Answer:
                                %s

                                Your task is to ask the next practice question.

                                Rules:
                                - If the user's last answer explicitly asks to skip or change the topic: pick a genuinely new, unrelated topic direction.
                                - If the user's last answer indicates confusion: return a simpler, shorter rewording of the last question.
                                - Otherwise, ask EXACTLY ONE simple follow-up question.
                                - NEVER repeat or rephrase any previously asked question.
                                - Avoid circling back to the same theme repeatedly.
                                - Vary the question's structure and phrasing.
                                - Explore different aspects of the topic.
                                - Use the user's last answer to judge their comfort level.
                                - Ask the question directly.
                                - Use easy and natural English.
                                - Keep the question under 20 words.
                                - Return ONLY the question text.
                                - Do not provide explanations, corrections, feedback, numbering, or multiple questions.
                                """,
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
                  "model": "openai/gpt-oss-120b",
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
              "model": "openai/gpt-oss-120b",
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
                - 1 to 3 sentences.
                - Return only the answer.
                """,
                question,
                mode,
                difficultyLevel
        );

        return callGroq(prompt);
    }

    // =========================================================
    // CUSTOM PRACTICE
    // =========================================================

    @Override
    public AiQuestionExtractionDto extractCustomPracticeQuestions(
            String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Question content cannot be empty."
            );
        }

        try {

            String prompt = """
                    You are a question extraction system.

                    The user will provide raw, unstructured text containing
                    one or more questions they want to practice.

                    USER CONTENT:
                    %s

                    Your task is to identify and extract every actual question.

                    IMPORTANT RULES:
                    - The input may contain numbered questions, bullet points,
                      commas, semicolons, line breaks, paragraphs, or inconsistent formatting.
                    - Questions may not always end with a question mark.
                    - Use meaning and context to determine question boundaries.
                    - Clean grammar and formatting when necessary.
                    - Preserve the original meaning of every question.
                    - Do NOT answer any question.
                    - Do NOT invent new questions.
                    - Do NOT silently remove duplicate or similar questions.
                    - Preserve logically connected compound questions as ONE question.
                    - Do NOT aggressively split related concepts that form one natural question.
                    - Remove numbering, bullet symbols, and unnecessary prefixes such as "Q:", "Question:", etc.
                    - Preserve the original question order.
                    - Return at most 100 questions.
                    - Every returned item must contain one complete practice question.

                    Return ONLY valid JSON in exactly this structure:

                    {
                      "questions": [
                        "Question 1",
                        "Question 2"
                      ]
                    }

                    Do not return markdown.
                    Do not return code fences.
                    Do not return explanations.
                    Do not return any text before or after the JSON.
                    """.formatted(content);

            String aiResponse = callGroq(prompt);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(aiResponse);
            JsonNode questionsNode = root.path("questions");

            if (!questionsNode.isArray()) {
                throw new IllegalStateException(
                        "Groq returned an invalid question extraction response."
                );
            }

            List<String> questions = new ArrayList<>();

            for (JsonNode questionNode : questionsNode) {

                String question = questionNode.asText().trim();

                if (!question.isBlank()) {
                    questions.add(question);
                }
            }

            if (questions.isEmpty()) {
                throw new IllegalStateException(
                        "No questions could be extracted from the provided content."
                );
            }

            if (questions.size() > 100) {
                throw new IllegalStateException(
                        "A maximum of 100 questions can be extracted per practice session."
                );
            }

            return new AiQuestionExtractionDto(questions);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to extract custom practice questions: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public AiAnswerEvaluationDto evaluateCustomPracticeAnswer(
            String question,
            String answer) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty."
            );
        }

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException(
                    "Answer cannot be empty."
            );
        }

        try {

            String prompt = """
                    You are an expert evaluator assessing whether a user's
                    recalled answer to a question is factually correct.

                    QUESTION:
                    %s

                    USER ANSWER:
                    %s

                    Evaluate ONLY the answer to the current question.

                    Do not use or assume any previous or future questions.

                    CLASSIFICATION:

                    CORRECT
                    - The core answer is factually accurate.
                    - Minor omissions that do not affect the main concept are acceptable.

                    PARTIALLY_CORRECT
                    - The user demonstrates correct understanding,
                      but an important concept or detail is missing,
                      incomplete, or partly inaccurate.

                    INCORRECT
                    - The core concept is wrong.
                    - The answer does not actually answer the question.
                    - The user says they do not know or cannot remember.
                    - The response contains no meaningful attempt.

                    SCORING:
                    - Return an integer score from 0 to 100.
                    - CORRECT should normally score 75-100.
                    - PARTIALLY_CORRECT should normally score 40-74.
                    - INCORRECT should normally score 0-39.
                    - Base the score on factual correctness and completeness.
                    - Do not reward verbosity.

                    FEEDBACK:
                    - Feedback must be between 20 and 35 words.
                    - Be direct and educational.
                    - If correct, briefly confirm why.
                    - If partially correct, explain what important concept is missing or inaccurate.
                    - If incorrect, explain the correct concept/answer.
                    - The correct answer may be revealed immediately.
                    - Do not ask another question.
                    - Do not use motivational filler.
                    - Do not mention the numeric score inside the feedback.

                    Return ONLY valid JSON in exactly this structure:

                    {
                      "status": "CORRECT",
                      "score": 90,
                      "feedback": "..."
                    }

                    status MUST be exactly one of:
                    CORRECT
                    PARTIALLY_CORRECT
                    INCORRECT

                    Do not return markdown.
                    Do not return code fences.
                    Do not return any text before or after the JSON.
                    """.formatted(question, answer);

            String aiResponse = callGroq(prompt);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(aiResponse);

            String statusValue = root
                    .path("status")
                    .asText()
                    .trim()
                    .toUpperCase();

            int score = root
                    .path("score")
                    .asInt(-1);

            String feedback = root
                    .path("feedback")
                    .asText()
                    .trim();

            AnswerEvaluationStatus status;

            try {
                status = AnswerEvaluationStatus.valueOf(statusValue);

            } catch (IllegalArgumentException e) {

                throw new IllegalStateException(
                        "Groq returned an invalid evaluation status."
                );
            }

            if (score < 0 || score > 100) {
                throw new IllegalStateException(
                        "Groq returned an invalid evaluation score."
                );
            }

            if (feedback.isBlank()) {
                throw new IllegalStateException(
                        "Groq returned empty evaluation feedback."
                );
            }

            validateEvaluationConsistency(status, score);

            return new AiAnswerEvaluationDto(
                    status,
                    score,
                    feedback
            );

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to evaluate custom practice answer: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public String generateCustomPracticeOverallFeedback(
            String practiceSummary) {

        if (practiceSummary == null || practiceSummary.isBlank()) {
            throw new IllegalArgumentException(
                    "Practice summary cannot be empty."
            );
        }

        String prompt = """
                You are an expert learning and revision coach.

                Analyze the following completed or partial practice session.

                PRACTICE SUMMARY:
                %s

                Generate a concise overall assessment for the user.

                Rules:
                - Focus on knowledge recall and answer accuracy.
                - Identify the user's strongest areas.
                - Identify concepts that require revision.
                - Pay attention to questions that were incorrect,
                  partially correct, skipped, or required a second attempt.
                - If the session ended early, do not judge unanswered questions
                  as incorrect.
                - Give practical revision recommendations.
                - Do not invent topics or performance information
                  that is not present in the summary.
                - Do not recalculate or contradict the provided scores.
                - Keep the response between 70 and 130 words.
                - Use clear, concise language.
                - Do not use markdown headings.
                - Do not return JSON.
                - Return only the assessment text.
                """.formatted(practiceSummary);

        String feedback = callGroq(prompt);

        if (feedback == null || feedback.isBlank()) {
            throw new IllegalStateException(
                    "Groq returned empty overall practice feedback."
            );
        }

        return feedback.trim();
    }

    private void validateEvaluationConsistency(
            AnswerEvaluationStatus status,
            int score) {

        switch (status) {

            case CORRECT -> {

                if (score < 75) {
                    throw new IllegalStateException(
                            "CORRECT evaluation cannot have a score below 75."
                    );
                }
            }

            case PARTIALLY_CORRECT -> {

                if (score < 40 || score > 74) {
                    throw new IllegalStateException(
                            "PARTIALLY_CORRECT evaluation must have a score between 40 and 74."
                    );
                }
            }

            case INCORRECT -> {

                if (score > 39) {
                    throw new IllegalStateException(
                            "INCORRECT evaluation cannot have a score above 39."
                    );
                }
            }
        }
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
              "model": "openai/gpt-oss-120b",
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