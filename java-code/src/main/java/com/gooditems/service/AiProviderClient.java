package com.gooditems.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooditems.config.AppProperties;
import com.gooditems.dto.AiProviderAnalysis;
import com.gooditems.exception.ApiException;
import com.gooditems.model.AiModelConfig;
import com.gooditems.model.Category;
import com.gooditems.model.MediaAsset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiProviderClient {
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_MAP = new ParameterizedTypeReference<>() {};
    private final AppProperties properties;
    private final ObjectMapper mapper;
    private final RestClient restClient;

    public AiProviderClient(AppProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
        this.restClient = RestClient.builder().build();
    }

    public AiProviderAnalysis analyzeImage(AiModelConfig model, MediaAsset media, byte[] imageBytes,
                                           String systemPrompt, List<Category> categories) {
        String apiKey = apiKey(model);
        String instruction = buildInstruction(categories);
        if ("doubao".equals(model.providerCode())) {
            return analyzeImageWithResponsesApi(model, media, apiKey, systemPrompt, instruction);
        }
        return analyzeImageWithChatCompletions(model, media, imageBytes, apiKey, systemPrompt, instruction);
    }

    private AiProviderAnalysis analyzeImageWithChatCompletions(AiModelConfig model, MediaAsset media, byte[] imageBytes,
                                                               String apiKey, String systemPrompt, String instruction) {
        String dataUrl = "data:%s;base64,%s".formatted(media.mimeType(), Base64.getEncoder().encodeToString(imageBytes));
        Map<String, Object> body = Map.of(
                "model", model.modelName(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", instruction),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        ))
                ),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );
        Map<String, Object> response = restClient.post()
                .uri(trimSlash(model.baseUrl()) + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(apiKey))
                .body(body)
                .retrieve()
                .body(RESPONSE_MAP);
        if (response == null) {
            throw new ApiException(502, "AI model returned no result");
        }
        return parseAnalysisResponse(response, extractChatContent(response));
    }

    private AiProviderAnalysis analyzeImageWithResponsesApi(AiModelConfig model, MediaAsset media, String apiKey,
                                                            String systemPrompt, String instruction) {
        Map<String, Object> body = Map.of(
                "model", model.modelName(),
                "input", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "input_image", "image_url", media.publicUrl()),
                                Map.of("type", "input_text", "text", instruction)
                        ))
                ),
                "temperature", 0.2
        );
        Map<String, Object> response = restClient.post()
                .uri(trimSlash(model.baseUrl()) + "/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(apiKey))
                .body(body)
                .retrieve()
                .body(RESPONSE_MAP);
        if (response == null) {
            throw new ApiException(502, "AI model returned no result");
        }
        return parseAnalysisResponse(response, extractResponsesContent(response));
    }

    private String buildInstruction(List<Category> categories) {
        String categoryText = categories.stream()
                .map(c -> "- id=%d, name=%s, slug=%s, description=%s".formatted(
                        c.id(), c.name(), c.slug(), c.description()))
                .reduce("", (a, b) -> a + b + "\n");
        return """
                Analyze the main item in this image and decide whether it belongs to an existing category
                or needs a new category. Existing categories:
                %s

                Return strict JSON only. Do not return markdown or any text outside JSON.
                Values should be written in Chinese where appropriate.
                Required fields:
                itemTitle, summary, experience, tags, decision, categoryId, newCategoryName,
                newCategorySlug, newCategoryDescription, confidence, reason.
                decision must be EXISTING_CATEGORY or NEW_CATEGORY.
                If using an existing category, categoryId must be one of the existing category ids.
                If creating a new category, provide a short Chinese category name and an English lowercase slug.
                confidence must be a number from 0 to 1.
                """.formatted(categoryText);
    }

    private AiProviderAnalysis parseAnalysisResponse(Map<String, Object> response, String content) {
        Map<String, Object> parsed = parseJsonContent(content);
        Map<String, Object> usage = map(response.get("usage"));
        int promptTokens = intValue(usage.get("prompt_tokens"));
        if (promptTokens == 0) {
            promptTokens = intValue(usage.get("input_tokens"));
        }
        int completionTokens = intValue(usage.get("completion_tokens"));
        if (completionTokens == 0) {
            completionTokens = intValue(usage.get("output_tokens"));
        }
        int totalTokens = intValue(usage.get("total_tokens"));
        if (totalTokens == 0) {
            totalTokens = promptTokens + completionTokens;
        }
        return new AiProviderAnalysis(
                string(parsed.get("itemTitle")),
                string(parsed.get("summary")),
                string(parsed.get("experience")),
                stringList(parsed.get("tags")),
                string(parsed.get("decision")),
                longValue(parsed.get("categoryId")),
                string(parsed.get("newCategoryName")),
                string(parsed.get("newCategorySlug")),
                string(parsed.get("newCategoryDescription")),
                decimal(parsed.get("confidence")),
                string(parsed.get("reason")),
                content,
                promptTokens,
                completionTokens,
                totalTokens
        );
    }

    private String apiKey(AiModelConfig model) {
        String value = switch (model.providerCode()) {
            case "kimi" -> properties.getAi().getKimiApiKey();
            case "doubao" -> properties.getAi().getDoubaoApiKey();
            default -> System.getenv(model.apiKeyEnv());
        };
        if (value == null || value.isBlank()) {
            throw new ApiException(500, "%s API key is not configured in %s".formatted(
                    model.displayName(), model.apiKeyEnv()));
        }
        return value;
    }

    private String extractChatContent(Map<String, Object> response) {
        List<?> choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new ApiException(502, "AI response is missing choices");
        }
        Map<String, Object> choice = map(choices.getFirst());
        Map<String, Object> message = map(choice.get("message"));
        String content = string(message.get("content"));
        if (content == null || content.isBlank()) {
            throw new ApiException(502, "AI response content is empty");
        }
        return cleanJsonText(content);
    }

    private String extractResponsesContent(Map<String, Object> response) {
        String outputText = string(response.get("output_text"));
        if (outputText != null && !outputText.isBlank()) {
            return cleanJsonText(outputText);
        }
        List<?> output = (List<?>) response.get("output");
        if (output != null) {
            for (Object item : output) {
                Map<String, Object> outputItem = map(item);
                List<?> contentList = (List<?>) outputItem.get("content");
                if (contentList == null) {
                    continue;
                }
                for (Object contentItem : contentList) {
                    Map<String, Object> content = map(contentItem);
                    String text = string(content.get("text"));
                    if (text == null || text.isBlank()) {
                        text = string(content.get("output_text"));
                    }
                    if (text != null && !text.isBlank()) {
                        return cleanJsonText(text);
                    }
                }
            }
        }
        throw new ApiException(502, "AI response content is empty");
    }

    private Map<String, Object> parseJsonContent(String content) {
        try {
            return mapper.readValue(content, MAP);
        } catch (Exception e) {
            throw new ApiException(502, "AI model did not return valid JSON");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(s -> !s.isBlank()).limit(8).toList();
        }
        return List.of();
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null || String.valueOf(value).isBlank() ? null : Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private BigDecimal decimal(Object value) {
        BigDecimal result;
        if (value instanceof Number number) {
            result = BigDecimal.valueOf(number.doubleValue());
        } else {
            try {
                result = value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
            } catch (Exception e) {
                result = BigDecimal.ZERO;
            }
        }
        if (result.compareTo(BigDecimal.ONE) > 0) {
            result = result.divide(BigDecimal.valueOf(100));
        }
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return result.compareTo(BigDecimal.ONE) > 0 ? BigDecimal.ONE : result;
    }

    private String cleanJsonText(String value) {
        return value.replace("```json", "").replace("```", "").trim();
    }

    private String trimSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
