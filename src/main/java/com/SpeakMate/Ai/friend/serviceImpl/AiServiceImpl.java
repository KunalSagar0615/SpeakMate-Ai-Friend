package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.config.GroqConfig;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import com.SpeakMate.Ai.friend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                                You are a technical interviewer.
                
                                Question:
                                %s
                
                                Candidate Answer:
                                %s
                
                                Evaluate the answer.
                
                                Rules:
                                - If the candidate explicitly asks to skip or change the topic/question (not confusion, a deliberate request): reply only with a brief 5-12 word acknowledgment like "No problem, let's move to a different question." Do not evaluate anything.
                                - If the candidate indicates confusion or says they didn't understand the question (not the same as not knowing the answer): reply only with a brief 5-15 word acknowledgment like "No worries, let me put that more simply." Do NOT restate or re-explain the question content yourself.
                                - If the candidate says they don't know the answer, asks you to tell them the answer, or gives no real attempt: respond with "Your answer is incorrect. Correct answer: <brief correct concept explanation>."
                                - Otherwise, first give a one-word verdict: Correct, Partially Correct, or Incorrect.
                                - Focus mainly on whether the core CONCEPT is correct. Only briefly touch on how it was worded.
                                - If Correct: confirm briefly and add one extra detail or edge case only if useful.
                                - If Partially Correct or Incorrect: state what concept is missing or wrong, then give the correct concept briefly.
                                - Do not use generic filler phrases like "Good attempt", "Needs improvement", "That's a great point", or "It's great that".
                                - Do not repeat the full question or answer back.
                                - Keep feedback between 15 and 40 words total. Be direct and concise.
                                - Do not ask a new question.
                                - Return feedback only.
                
                                Format:
                
                                Verdict: <Correct/Partially Correct/Incorrect>
                                Feedback: <short, concept-focused feedback>
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
                                - If the user explicitly asks to skip or change the topic/question: reply only with a brief 5-12 word acknowledgment like "Sure, let's try something else." Do not evaluate anything.
                                - If the user indicates confusion or says they didn't understand the question: reply only with a brief 5-15 word acknowledgment like "No worries, let me simplify that." Do NOT restate the question yourself.
                                - If the user says they don't know, gives no real attempt, or the answer is empty/off-topic: respond with "Your answer is incorrect. Correct answer: <a short, natural example answer in English>."
                                - Otherwise check BOTH: (1) grammar correctness, and (2) whether the answer actually makes sense / answers the question.
                                - If grammar is correct and content makes sense, say so briefly — do not invent mistakes.
                                - If there are grammar issues, correct them.
                                - If the content doesn't really answer the question or is unclear, point that out briefly too.
                                - Do not use generic filler phrases like "That's a great point" or "It's great that".
                                - Respond in 15 to 40 words total. Do not exceed 40 words.
                                - Do not ask another question.
                                - Return feedback only.
                
                                Format:
                
                                Corrected Sentence: <only if grammar needed correcting, else write "None needed">
                                Feedback: <short note covering grammar + whether the answer made sense>
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
                
                                Your task is to react briefly to the user's response.
                
                                Rules:
                                - If the user explicitly asks to skip, change the topic, or move to something else: reply only with a brief 5-12 word acknowledgment like "Sure, let's talk about something else." Nothing more.
                                - If the user says they didn't understand the question, seems confused, or asks you to explain/repeat it: reply only with a brief 5-15 word acknowledgment like "No worries, let me simplify that for you." Do NOT restate or answer the question yourself — a simpler version of the question will be asked separately.
                                - If the user says they don't know or gives no real attempt: gently tell them the concept/answer in a friendly tone, without sounding like a teacher.
                                - Otherwise, react naturally and specifically to what they said.
                                - Respond in 15 to 40 words total. Do not exceed 40 words (except the short acknowledgments above, which must stay under 15 words).
                                - Do NOT ask any question.
                                - Do NOT end with a question mark.
                                - Do NOT evaluate, score, or judge the user.
                                - Do NOT use generic filler phrases like "That's a great point", "That's awesome", "It's great that", "You seem to be" — respond naturally and specifically instead.
                                - Keep it warm, casual, and human — like a real friend replying in chat, not motivational commentary.
                
                                English Improvement Rules:
                                - If the response is understandable and mostly grammatically correct, just react naturally — do not rewrite it.
                                - If the response has major grammar mistakes or broken structure (more than 50%% incorrect), silently rewrite it into a natural, correct version instead of your normal reaction.
                                - Do NOT add any label like "Natural English Version:" — give the corrected sentence directly as your response.
                                - Do NOT explain grammar rules.
                                - Do NOT mention words like "grammar", "incorrect", "wrong", or "error".
                
                                Return only the response text, nothing else.
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
                                - Ask the question directly — do NOT add a reflective/empathetic preamble commenting on the previous answer (e.g. no "That's interesting" or "Great point" before the question).
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
                                - If the user's last answer indicates confusion (e.g. "I didn't get your question", "I don't understand", "can you repeat", "explain simply"): take the LAST question from Previously Asked Questions and return a simpler, shorter rewording of that exact same question in a casual tone. Never claim no previous question exists — the list always contains one.
                                - Otherwise, ask EXACTLY ONE follow-up question.
                                - NEVER repeat or rephrase any previously asked question.
                                - Avoid circling back to the same theme or subtopic repeatedly (e.g. exercise, daily routine, obstacles) — after 1-2 questions on a theme, move to a genuinely different one.
                                - Vary the question's structure/phrasing style — avoid repeatedly using the same sentence template (e.g. "How do you think X influences Y?") across consecutive questions.
                                - Use the user's last answer to pick a natural next direction — react to what they actually said and steer toward a related but new aspect of the topic, the way a real friend would in conversation.
                                - Ask the question directly — do NOT prefix it with a reflective/empathetic comment on the previous answer (e.g. no "That's really impressive that..." or "That's a really powerful image..." before the question).
                                - Keep the question under 20 words, single sentence, no compound double-clause questions.
                                - Sound casual, warm, and human — not like a survey.
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
                                - If the user's last answer explicitly asks to skip or change the topic: pick a genuinely new, unrelated topic direction and ask a fresh simple question about it.
                                - If the user's last answer indicates confusion (e.g. "I didn't get your question", "I don't understand", "can you repeat"): take the LAST question from Previously Asked Questions and return a simpler, shorter rewording of that exact same question. Never claim no previous question exists — the list always contains one.
                                - Otherwise, ask EXACTLY ONE simple follow-up question.
                                - NEVER repeat or rephrase any previously asked question.
                                - Avoid circling back to the same theme repeatedly — after 1-2 questions on a theme, move to a genuinely different one.
                                - Vary the question's structure/phrasing style — avoid repeatedly using the same sentence template across consecutive questions.
                                - Explore a different aspect of the topic to build vocabulary, fluency, confidence, and sentence formation.
                                - Use the user's last answer to judge their comfort level: if their answer was short, broken, or hesitant, keep the next question simple and easy to answer; if their answer was fluent and confident, ask something slightly more expressive or descriptive.
                                - Ask the question directly — do NOT add a reflective preamble before it.
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