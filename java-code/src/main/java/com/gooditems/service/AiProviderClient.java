package com.gooditems.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooditems.config.AppProperties;
import com.gooditems.dto.AiProviderAnalysis;
import com.gooditems.exception.ApiException;
import com.gooditems.model.AiModelConfig;
import com.gooditems.model.Category;
import com.gooditems.model.MediaAsset;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
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
        String categoryText = categories.stream()
                .map(c -> "- id=%d, name=%s, slug=%s, description=%s".formatted(c.id(), c.name(), c.slug(), c.description()))
                .reduce("", (a, b) -> a + b + "\n");
        String instruction = """
                请分析图片中的主要物品，并根据已有分类判断归类。
                已有分类：
                %s
                返回严格 JSON，字段必须包括：
                itemTitle, summary, experience, tags, decision, categoryId, newCategoryName,
                newCategorySlug, newCategoryDescription, confidence, reason。
                decision 只能是 EXISTING_CATEGORY 或 NEW_CATEGORY。
                如果选择已有分类，categoryId 必须是已有分类 id。
                如果新增分类，请给出简短中文分类名和英文小写 slug。
                不要输出 Markdown，不要输出 JSON 以外的文字。
                """.formatted(categoryText);
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
            throw new ApiException(502, "AI 模型没有返回结果");
        }
        String content = extractContent(response);
        Map<String, Object> parsed = parseJsonContent(content);
        Map<String, Object> usage = map(response.get("usage"));
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
                intValue(usage.get("prompt_tokens")),
                intValue(usage.get("completion_tokens")),
                intValue(usage.get("total_tokens"))
        );
    }

    private String apiKey(AiModelConfig model) {
        String value = switch (model.providerCode()) {
            case "kimi" -> properties.getAi().getKimiApiKey();
            case "doubao" -> properties.getAi().getDoubaoApiKey();
            default -> System.getenv(model.apiKeyEnv());
        };
        if (value == null || value.isBlank()) {
            throw new ApiException(500, "%s 的 API Key 未配置，请在服务器环境变量中配置 %s".formatted(model.displayName(), model.apiKeyEnv()));
        }
        return value;
    }

    private String extractContent(Map<String, Object> response) {
        List<?> choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new ApiException(502, "AI 模型返回格式异常：缺少 choices");
        }
        Map<String, Object> choice = map(choices.getFirst());
        Map<String, Object> message = map(choice.get("message"));
        String content = string(message.get("content"));
        if (content == null || content.isBlank()) {
            throw new ApiException(502, "AI 模型返回内容为空");
        }
        return content.replace("```json", "").replace("```", "").trim();
    }

    private Map<String, Object> parseJsonContent(String content) {
        try {
            return mapper.readValue(content, MAP);
        } catch (Exception e) {
            throw new ApiException(502, "AI 模型没有返回合法 JSON，请转人工确认");
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
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String trimSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
