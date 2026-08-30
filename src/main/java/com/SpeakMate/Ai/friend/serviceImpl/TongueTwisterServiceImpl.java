package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.config.GroqConfig;
import com.SpeakMate.Ai.friend.dto.TongueTwisterHistoryDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterResponseDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterStatsDto;
import com.SpeakMate.Ai.friend.entities.TongueTwisterPassage;
import com.SpeakMate.Ai.friend.entities.TongueTwisterSession;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.SessionStatus;
import com.SpeakMate.Ai.friend.repository.TongueTwisterPassageRepository;
import com.SpeakMate.Ai.friend.repository.TongueTwisterSessionRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.TongueTwisterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TongueTwisterServiceImpl implements TongueTwisterService {

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String GROQ_MODEL =
            "openai/gpt-oss-120b";

    private static final int MIN_WORD_COUNT = 50;

    private static final int MAX_WORD_COUNT = 70;

    private static final int MAX_GENERATION_ATTEMPTS = 3;

    private static final double SIMILARITY_THRESHOLD = 0.85;

    private final TongueTwisterSessionRepository tongueTwisterSessionRepository;

    private final TongueTwisterPassageRepository tongueTwisterPassageRepository;

    private final UserRepository userRepository;

    private final GroqConfig groqConfig;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public TongueTwisterServiceImpl(
            TongueTwisterSessionRepository tongueTwisterSessionRepository,
            TongueTwisterPassageRepository tongueTwisterPassageRepository,
            UserRepository userRepository,
            GroqConfig groqConfig,
            RestTemplate restTemplate
    ) {
        this.tongueTwisterSessionRepository =
                tongueTwisterSessionRepository;

        this.tongueTwisterPassageRepository =
                tongueTwisterPassageRepository;

        this.userRepository =
                userRepository;

        this.groqConfig =
                groqConfig;

        this.restTemplate =
                restTemplate;

        this.objectMapper =
                new ObjectMapper();
    }

    // =========================================================
    // START SESSION
    // =========================================================

    @Override
    @Transactional
    public TongueTwisterResponseDto startSession() {

        User user = getLoggedInUser();

        /*
         * If the user already has an active Tongue Twister session,
         * return that session instead of creating duplicate active
         * sessions.
         */
        TongueTwisterSession existingSession =
                tongueTwisterSessionRepository
                        .findFirstByUserIdAndStatusOrderByStartTimeDesc(
                                user.getId(),
                                SessionStatus.ACTIVE
                        )
                        .orElse(null);

        if (existingSession != null) {

            TongueTwisterPassage latestPassage =
                    tongueTwisterPassageRepository
                            .findTop15ByUserIdOrderByCreatedAtDesc(
                                    user.getId()
                            )
                            .stream()
                            .findFirst()
                            .orElse(null);

            if (latestPassage != null) {
                return buildResponse(
                        existingSession,
                        latestPassage,
                        user
                );
            }

            TongueTwisterPassage passage =
                    generateAndSavePassage(user);

            return buildResponse(
                    existingSession,
                    passage,
                    user
            );
        }

        TongueTwisterSession session =
                new TongueTwisterSession();

        session.setUser(user);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.ACTIVE);

        session =
                tongueTwisterSessionRepository.save(session);

        TongueTwisterPassage passage =
                generateAndSavePassage(user);

        return buildResponse(
                session,
                passage,
                user
        );
    }

    // =========================================================
    // GENERATE NEW PASSAGE
    // =========================================================

    @Override
    @Transactional
    public TongueTwisterResponseDto generateNewPassage(
            Long sessionId
    ) {

        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID cannot be null."
            );
        }

        User user = getLoggedInUser();

        TongueTwisterSession session =
                tongueTwisterSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tongue Twister session not found."
                                )
                        );

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException(
                    "You are not authorized to use this session."
            );
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "This Tongue Twister session is no longer active."
            );
        }

        TongueTwisterPassage passage =
                generateAndSavePassage(user);

        return buildResponse(
                session,
                passage,
                user
        );
    }

    // =========================================================
    // END SESSION
    // =========================================================

    @Override
    @Transactional
    public TongueTwisterResponseDto endSession(
            Long sessionId
    ) {

        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID cannot be null."
            );
        }

        User user = getLoggedInUser();

        TongueTwisterSession session =
                tongueTwisterSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tongue Twister session not found."
                                )
                        );

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException(
                    "You are not authorized to use this session."
            );
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {

            TongueTwisterPassage latestPassage =
                    getLatestPassage(user);

            return buildResponse(
                    session,
                    latestPassage,
                    user
            );
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        session =
                tongueTwisterSessionRepository.save(session);

        TongueTwisterPassage latestPassage =
                getLatestPassage(user);

        return buildResponse(
                session,
                latestPassage,
                user
        );
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TongueTwisterStatsDto getStats() {

        User user = getLoggedInUser();

        long totalSessions = tongueTwisterSessionRepository.countByUserId(user.getId());

        long completedSessions = tongueTwisterSessionRepository.countByUserIdAndStatus(user.getId(), SessionStatus.COMPLETED);

        long cancelledSessions = tongueTwisterSessionRepository.countByUserIdAndStatus(user.getId(), SessionStatus.CANCELLED);

        long totalPassages = tongueTwisterPassageRepository.countByUserId(user.getId());

        return new TongueTwisterStatsDto(totalSessions, completedSessions, cancelledSessions, totalPassages);
    }

    // =========================================================
    // HISTORY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TongueTwisterHistoryDto> getHistory() {

        User user = getLoggedInUser();

        List<TongueTwisterPassage> passages = tongueTwisterPassageRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<TongueTwisterHistoryDto> history = new ArrayList<>();

        for (TongueTwisterPassage passage : passages) {

            history.add( new TongueTwisterHistoryDto(
                            passage.getId(),
                            passage.getPassage(),
                            passage.getWordCount(),
                            passage.getCreatedAt()
                    )
            );
        }

        return history;
    }

    // =========================================================
    // DOWNLOAD HISTORY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public String downloadHistory() {

        User user = getLoggedInUser();

        List<TongueTwisterPassage> passages =
                tongueTwisterPassageRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        StringBuilder content =
                new StringBuilder();

        content.append(
                "PREPFRIEND - TONGUE TWISTER HISTORY\n"
        );

        content.append(
                "====================================\n\n"
        );

        content.append(
                "User: "
        );

        content.append(user.getUsername());

        content.append("\n");

        content.append(
                "Total Passages: "
        );

        content.append(passages.size());

        content.append("\n\n");

        if (passages.isEmpty()) {

            content.append(
                    "No tongue twister passages found.\n"
            );

            return content.toString();
        }

        int number = 1;

        for (TongueTwisterPassage passage : passages) {

            content.append(
                    "Passage "
            );

            content.append(number);

            content.append("\n");

            content.append(
                    "Date: "
            );

            content.append(
                    passage.getCreatedAt()
            );

            content.append("\n");

            content.append(
                    "Word Count: "
            );

            content.append(
                    passage.getWordCount()
            );

            content.append("\n\n");

            content.append(
                    passage.getPassage()
            );

            content.append("\n");

            content.append(
                    "------------------------------------\n\n"
            );

            number++;
        }

        return content.toString();
    }

    // =========================================================
    // GENERATE AND SAVE PASSAGE
    // =========================================================

    private TongueTwisterPassage generateAndSavePassage(
            User user
    ) {

        List<TongueTwisterPassage> recentPassages =
                tongueTwisterPassageRepository
                        .findTop15ByUserIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        String passage =
                generateUniqueTongueTwister(
                        recentPassages
                );

        int wordCount =
                countWords(passage);

        TongueTwisterPassage entity =
                new TongueTwisterPassage();

        entity.setUser(user);
        entity.setPassage(passage);
        entity.setWordCount(wordCount);
        entity.setCreatedAt(LocalDateTime.now());

        return tongueTwisterPassageRepository.save(entity);
    }

    // =========================================================
    // GROQ GENERATION + UNIQUENESS CHECK
    // =========================================================

    private String generateUniqueTongueTwister(
            List<TongueTwisterPassage> recentPassages
    ) {

        String previousPassages =
                buildPreviousPassagesContext(
                        recentPassages
                );

        String lastGeneratedPassage = null;

        for (
                int attempt = 1;
                attempt <= MAX_GENERATION_ATTEMPTS;
                attempt++
        ) {

            String prompt =
                    buildTongueTwisterPrompt(
                            previousPassages,
                            attempt
                    );

            String generated =
                    callGroq(prompt);

            generated =
                    cleanGeneratedPassage(generated);

            if (generated.isBlank()) {
                continue;
            }

            int wordCount =
                    countWords(generated);

            if (
                    wordCount < MIN_WORD_COUNT
                            || wordCount > MAX_WORD_COUNT
            ) {

                lastGeneratedPassage =
                        generated;

                continue;
            }

            if (
                    isTooSimilarToRecentPassages(
                            generated,
                            recentPassages
                    )
            ) {

                lastGeneratedPassage =
                        generated;

                continue;
            }

            return generated;
        }

        throw new IllegalStateException(
                "Unable to generate a sufficiently unique "
                        + "50–70 word tongue twister after "
                        + MAX_GENERATION_ATTEMPTS
                        + " attempts."
                        + (
                        lastGeneratedPassage != null
                                ? " Please try again."
                                : ""
                )
        );
    }

    // =========================================================
    // GROQ PROMPT
    // =========================================================

    private String buildTongueTwisterPrompt(
            String previousPassages,
            int attempt
    ) {

        String retryInstruction = "";

        if (attempt > 1) {

            retryInstruction = """
                    
                    This is a retry because the previous generated
                    passage did not satisfy the requirements.
                    Be substantially different from the previous
                    attempts and the historical passages.
                    """;
        }

        return """
                You are an expert English pronunciation and
                communication-practice content creator.

                Create ONE difficult English tongue-twister
                reading passage for a learner who wants to improve
                pronunciation, fluency, clarity, and speaking speed.

                STRICT REQUIREMENTS:

                1. The passage MUST contain between 50 and 70 words.
                2. Make it challenging to read aloud.
                3. Use repeated consonant sounds, similar-sounding
                   words, alliteration, consonant clusters, and
                   difficult word combinations.
                4. The passage must still form a meaningful,
                   grammatically understandable passage.
                5. Do not simply create a list of unrelated words.
                6. Make the passage feel like a short connected
                   paragraph.
                7. Use different vocabulary and sentence structures.
                8. Do not copy, repeat, or closely imitate any
                   previous passage.
                9. Do not use numbering.
                10. Do not use quotation marks around the passage.
                11. Do not use markdown.
                12. Do not provide an introduction or explanation.
                13. Return ONLY the passage itself.
                14. The result MUST be between 50 and 70 words.

                PREVIOUS 15 PASSAGES:

                %s

                IMPORTANT:
                The previous passages are provided specifically to
                prevent repetition. Create a genuinely new passage
                with different wording, different sentence structure,
                and different combinations of difficult sounds.

                %s

                Return ONLY the new 50–70 word passage.
                """.formatted(
                previousPassages,
                retryInstruction
        );
    }

    // =========================================================
    // PREVIOUS PASSAGES CONTEXT
    // =========================================================

    private String buildPreviousPassagesContext(
            List<TongueTwisterPassage> recentPassages
    ) {

        if (
                recentPassages == null
                        || recentPassages.isEmpty()
        ) {

            return "No previous passages are available.";
        }

        StringBuilder context =
                new StringBuilder();

        int number = 1;

        /*
         * The repository returns newest first.
         */
        for (TongueTwisterPassage passage : recentPassages) {

            context.append(number)
                    .append(". ")
                    .append(passage.getPassage())
                    .append("\n\n");

            number++;
        }

        return context.toString();
    }

    // =========================================================
    // SIMILARITY CHECK
    // =========================================================

    private boolean isTooSimilarToRecentPassages(
            String newPassage,
            List<TongueTwisterPassage> recentPassages
    ) {

        if (
                recentPassages == null
                        || recentPassages.isEmpty()
        ) {
            return false;
        }

        String normalizedNew =
                normalizeText(newPassage);

        Set<String> newWords =
                extractMeaningfulWords(normalizedNew);

        for (TongueTwisterPassage oldPassage : recentPassages) {

            String normalizedOld =
                    normalizeText(
                            oldPassage.getPassage()
                    );

            if (
                    normalizedNew.equals(
                            normalizedOld
                    )
            ) {

                return true;
            }

            Set<String> oldWords =
                    extractMeaningfulWords(
                            normalizedOld
                    );

            double similarity =
                    calculateJaccardSimilarity(
                            newWords,
                            oldWords
                    );

            if (
                    similarity >= SIMILARITY_THRESHOLD
            ) {

                return true;
            }
        }

        return false;
    }

    private double calculateJaccardSimilarity(
            Set<String> first,
            Set<String> second
    ) {

        if (
                first.isEmpty()
                        && second.isEmpty()
        ) {
            return 1.0;
        }

        if (
                first.isEmpty()
                        || second.isEmpty()
        ) {
            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(first);

        intersection.retainAll(second);

        Set<String> union =
                new HashSet<>(first);

        union.addAll(second);

        return (double) intersection.size()
                / union.size();
    }

    private Set<String> extractMeaningfulWords(
            String text
    ) {

        Set<String> words =
                new HashSet<>();

        String[] tokens =
                text.split("\\s+");

        for (String token : tokens) {

            String cleaned =
                    token.replaceAll(
                            "[^a-zA-Z]",
                            ""
                    ).toLowerCase(Locale.ROOT);

            if (
                    !cleaned.isBlank()
                            && cleaned.length() > 2
            ) {

                words.add(cleaned);
            }
        }

        return words;
    }

    private String normalizeText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^a-zA-Z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    // =========================================================
    // CLEAN GROQ RESPONSE
    // =========================================================

    private String cleanGeneratedPassage(
            String passage
    ) {

        if (passage == null) {
            return "";
        }

        String cleaned =
                passage.trim();

        /*
         * Remove accidental markdown code fences.
         */
        cleaned =
                cleaned.replace(
                        "```text",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "```",
                        ""
                );

        /*
         * Remove accidental surrounding quotation marks.
         */
        if (
                cleaned.length() >= 2
                        && (
                        (
                                cleaned.startsWith("\"")
                                        && cleaned.endsWith("\"")
                        )
                                ||
                                (
                                        cleaned.startsWith("'")
                                                && cleaned.endsWith("'")
                                )
                )
        ) {

            cleaned =
                    cleaned.substring(
                            1,
                            cleaned.length() - 1
                    ).trim();
        }

        /*
         * Remove accidental leading labels.
         */
        cleaned =
                cleaned.replaceFirst(
                        "(?i)^passage\\s*:\\s*",
                        ""
                );

        cleaned =
                cleaned.replaceFirst(
                        "(?i)^tongue\\s*twister\\s*:\\s*",
                        ""
                );

        return cleaned.trim();
    }

    // =========================================================
    // WORD COUNT
    // =========================================================

    private int countWords(
            String text
    ) {

        if (
                text == null
                        || text.isBlank()
        ) {
            return 0;
        }

        return text
                .trim()
                .split("\\s+")
                .length;
    }

    // =========================================================
    // GROQ API CALL
    // =========================================================

    private String callGroq(
            String prompt
    ) {

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    groqConfig.getApiKey()
            );

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            String requestBody =
                    """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "user",
                          "content": %s
                        }
                      ]
                    }
                    """.formatted(
                            GROQ_MODEL,
                            objectMapper.writeValueAsString(
                                    prompt
                            )
                    );

            HttpEntity<String> entity =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            GROQ_URL,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            if (
                    response.getBody() == null
                            || response.getBody().isBlank()
            ) {

                throw new IllegalStateException(
                        "Groq returned an empty response."
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.getBody()
                    );

            JsonNode choices =
                    root.path("choices");

            if (
                    !choices.isArray()
                            || choices.isEmpty()
            ) {

                throw new IllegalStateException(
                        "Groq returned no choices."
                );
            }

            String content =
                    choices
                            .get(0)
                            .path("message")
                            .path("content")
                            .asText();

            if (
                    content == null
                            || content.isBlank()
            ) {

                throw new IllegalStateException(
                        "Groq returned empty passage content."
                );
            }

            return content.trim();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq Tongue Twister API Error: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // LOGGED-IN USER
    // =========================================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {

            throw new IllegalStateException(
                    "User is not authenticated."
            );
        }

        Object principal =
                authentication.getPrincipal();

        String username;

        if (principal instanceof UserDetails userDetails) {

            username =
                    userDetails.getUsername();

        } else if (principal instanceof String principalString) {

            username =
                    principalString;

        } else {

            throw new IllegalStateException(
                    "Unable to determine logged-in user."
            );
        }

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Logged-in user was not found."
                        )
                );
    }

    // =========================================================
    // LATEST PASSAGE
    // =========================================================

    private TongueTwisterPassage getLatestPassage(
            User user
    ) {

        return tongueTwisterPassageRepository
                .findTop15ByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    // =========================================================
    // RESPONSE BUILDER
    // =========================================================

    private TongueTwisterResponseDto buildResponse(
            TongueTwisterSession session,
            TongueTwisterPassage passage,
            User user
    ) {

        long completedSessions =
                tongueTwisterSessionRepository
                        .countByUserIdAndStatus(
                                user.getId(),
                                SessionStatus.COMPLETED
                        );

        long totalSessions =
                tongueTwisterSessionRepository
                        .countByUserId(
                                user.getId()
                        );

        if (passage == null) {

            return new TongueTwisterResponseDto(
                    session.getId(),
                    null,
                    null,
                    0,
                    completedSessions,
                    totalSessions
            );
        }

        return new TongueTwisterResponseDto(
                session.getId(),
                passage.getId(),
                passage.getPassage(),
                passage.getWordCount(),
                completedSessions,
                totalSessions
        );
    }
}