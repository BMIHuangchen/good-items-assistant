package com.gooditems.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooditems.dto.AiFeatureSettingsRequest;
import com.gooditems.dto.AiModelConfigRequest;
import com.gooditems.exception.ApiException;
import com.gooditems.model.AiCallLog;
import com.gooditems.model.AiFeatureSettings;
import com.gooditems.model.AiImageAnalysisTask;
import com.gooditems.model.AiModelConfig;
import com.gooditems.model.MediaAsset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class AiRepository {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public AiRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public AiFeatureSettings settings() {
        List<AiFeatureSettings> list = jdbc.query("select * from ai_feature_settings where id = 1", settingsMapper());
        if (!list.isEmpty()) {
            return list.getFirst();
        }
        jdbc.update("""
                insert into ai_feature_settings(id, ai_enabled, auto_ingest_enabled, auto_publish_enabled,
                low_confidence_review_enabled, confidence_threshold, daily_call_limit, max_image_size_mb)
                values(1,0,0,0,1,0.7500,50,5)
                """);
        return settings();
    }

    public AiFeatureSettings updateSettings(AiFeatureSettingsRequest request) {
        AiFeatureSettings current = settings();
        jdbc.update("""
                update ai_feature_settings
                set ai_enabled=?, auto_ingest_enabled=?, auto_publish_enabled=?, low_confidence_review_enabled=?,
                confidence_threshold=?, daily_call_limit=?, max_image_size_mb=?
                where id=1
                """,
                bool(request.aiEnabled(), current.aiEnabled()),
                bool(request.autoIngestEnabled(), current.autoIngestEnabled()),
                bool(request.autoPublishEnabled(), current.autoPublishEnabled()),
                bool(request.lowConfidenceReviewEnabled(), current.lowConfidenceReviewEnabled()),
                decimal(request.confidenceThreshold(), current.confidenceThreshold()),
                integer(request.dailyCallLimit(), current.dailyCallLimit()),
                integer(request.maxImageSizeMb(), current.maxImageSizeMb()));
        return settings();
    }

    public List<AiModelConfig> models(boolean enabledOnly) {
        String sql = enabledOnly
                ? "select * from ai_model_configs where enabled = 1 order by sort_order desc, id desc"
                : "select * from ai_model_configs order by sort_order desc, id desc";
        return jdbc.query(sql, modelMapper());
    }

    public AiModelConfig model(String providerCode) {
        return jdbc.query("select * from ai_model_configs where provider_code = ?", modelMapper(), providerCode)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "AI 模型配置不存在"));
    }

    public AiModelConfig updateModel(Long id, AiModelConfigRequest request) {
        jdbc.update("""
                update ai_model_configs set display_name=?, model_name=?, base_url=?, api_key_env=?, enabled=?,
                prompt_price_per_1k=?, completion_price_per_1k=?, sort_order=? where id=?
                """, request.displayName(), request.modelName(), trimSlash(request.baseUrl()), request.apiKeyEnv(),
                bool(request.enabled(), false), decimal(request.promptPricePer1k(), BigDecimal.ZERO),
                decimal(request.completionPricePer1k(), BigDecimal.ZERO), integer(request.sortOrder(), 0), id);
        return jdbc.query("select * from ai_model_configs where id = ?", modelMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "AI 模型配置不存在"));
    }

    public int todayCallCount() {
        Integer value = jdbc.queryForObject("""
                select count(*) from ai_call_logs
                where scenario = 'IMAGE_CLASSIFY' and created_at >= curdate()
                """, Integer.class);
        return value == null ? 0 : value;
    }

    public String prompt(String scenario) {
        return jdbc.query("select prompt_text from ai_prompt_templates where scenario = ? and enabled = 1", (rs, rowNum) -> rs.getString(1), scenario)
                .stream()
                .findFirst()
                .orElse("你是好物展示小助手，请分析图片中的物品并返回严格 JSON。");
    }

    public MediaAsset createMedia(String source, String originalFilename, String mimeType, long fileSize,
                                  String sha256, String objectKey, String publicUrl) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into media_assets(source, original_filename, mime_type, file_size, sha256, object_key, public_url)
                    values(?,?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, source);
            ps.setString(2, originalFilename);
            ps.setString(3, mimeType);
            ps.setLong(4, fileSize);
            ps.setString(5, sha256);
            ps.setString(6, objectKey);
            ps.setString(7, publicUrl);
            return ps;
        }, keyHolder);
        return media(keyHolder.getKey().longValue());
    }

    public MediaAsset media(Long id) {
        return jdbc.query("select * from media_assets where id = ?", mediaMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "图片资源不存在"));
    }

    public AiImageAnalysisTask createTask(String requestId, Long mediaAssetId, String providerCode, String modelName,
                                          String status, String ingestMode, Map<String, Object> result, String reviewReason) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into ai_image_analysis_tasks(request_id, media_asset_id, provider_code, model_name, status,
                    ingest_mode, item_title, summary, experience, tags_json, decision, matched_category_id,
                    new_category_name, new_category_slug, new_category_description, confidence, reason, review_reason, raw_result_json)
                    values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, requestId);
            ps.setLong(2, mediaAssetId);
            ps.setString(3, providerCode);
            ps.setString(4, modelName);
            ps.setString(5, status);
            ps.setString(6, ingestMode);
            ps.setString(7, string(result.get("itemTitle")));
            ps.setString(8, string(result.get("summary")));
            ps.setString(9, string(result.get("experience")));
            ps.setString(10, json(result.get("tags")));
            ps.setString(11, string(result.get("decision")));
            ps.setObject(12, longValue(result.get("categoryId")));
            ps.setString(13, string(result.get("newCategoryName")));
            ps.setString(14, string(result.get("newCategorySlug")));
            ps.setString(15, string(result.get("newCategoryDescription")));
            ps.setBigDecimal(16, decimalValue(result.get("confidence")));
            ps.setString(17, string(result.get("reason")));
            ps.setString(18, reviewReason);
            ps.setString(19, json(result));
            return ps;
        }, keyHolder);
        return task(keyHolder.getKey().longValue());
    }

    public AiImageAnalysisTask task(Long id) {
        return jdbc.query(taskSql() + " where t.id = ?", taskMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "AI 分析任务不存在"));
    }

    public List<AiImageAnalysisTask> tasks(String status, int pageSize) {
        if (status == null || status.isBlank()) {
            return jdbc.query(taskSql() + " order by t.updated_at desc limit ?", taskMapper(), pageSize);
        }
        return jdbc.query(taskSql() + " where t.status = ? order by t.updated_at desc limit ?", taskMapper(), status, pageSize);
    }

    public void markTaskIngested(Long taskId, String status, String ingestMode, Long categoryId, Long itemId) {
        jdbc.update("""
                update ai_image_analysis_tasks
                set status=?, ingest_mode=?, created_category_id=?, created_item_id=?, updated_at=now()
                where id=?
                """, status, ingestMode, categoryId, itemId, taskId);
    }

    public void rejectTask(Long taskId, String reason) {
        jdbc.update("update ai_image_analysis_tasks set status='REJECTED', review_reason=?, updated_at=now() where id=?",
                reason == null || reason.isBlank() ? "后台驳回" : reason, taskId);
    }

    public void createCallLog(String requestId, String providerCode, String modelName, String scenario, String status,
                              int promptTokens, int completionTokens, int totalTokens, BigDecimal estimatedCost,
                              int durationMs, String errorMessage, Long taskId) {
        jdbc.update("""
                insert into ai_call_logs(request_id, provider_code, model_name, scenario, status, prompt_tokens,
                completion_tokens, total_tokens, estimated_cost, duration_ms, error_message, task_id)
                values(?,?,?,?,?,?,?,?,?,?,?,?)
                """, requestId, providerCode, modelName, scenario, status, promptTokens, completionTokens,
                totalTokens, estimatedCost, durationMs, errorMessage, taskId);
    }

    public List<AiCallLog> callLogs(int pageSize) {
        return jdbc.query("select * from ai_call_logs order by created_at desc, id desc limit ?", callLogMapper(), pageSize);
    }

    private String taskSql() {
        return """
                select t.*, m.public_url media_url, c.name matched_category_name
                from ai_image_analysis_tasks t
                left join media_assets m on m.id = t.media_asset_id
                left join content_categories c on c.id = t.matched_category_id
                """;
    }

    private RowMapper<AiFeatureSettings> settingsMapper() {
        return (rs, rowNum) -> new AiFeatureSettings(rs.getLong("id"), rs.getBoolean("ai_enabled"),
                rs.getBoolean("auto_ingest_enabled"), rs.getBoolean("auto_publish_enabled"),
                rs.getBoolean("low_confidence_review_enabled"), rs.getBigDecimal("confidence_threshold"),
                rs.getInt("daily_call_limit"), rs.getInt("max_image_size_mb"), time(rs.getObject("updated_at")));
    }

    private RowMapper<AiModelConfig> modelMapper() {
        return (rs, rowNum) -> new AiModelConfig(rs.getLong("id"), rs.getString("provider_code"),
                rs.getString("display_name"), rs.getString("model_name"), rs.getString("base_url"),
                rs.getString("api_key_env"), rs.getBoolean("enabled"), rs.getBigDecimal("prompt_price_per_1k"),
                rs.getBigDecimal("completion_price_per_1k"), rs.getInt("sort_order"),
                time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<MediaAsset> mediaMapper() {
        return (rs, rowNum) -> new MediaAsset(rs.getLong("id"), rs.getString("source"),
                rs.getString("original_filename"), rs.getString("mime_type"), rs.getLong("file_size"),
                rs.getString("sha256"), rs.getString("object_key"), rs.getString("public_url"),
                time(rs.getObject("created_at")));
    }

    private RowMapper<AiImageAnalysisTask> taskMapper() {
        return (rs, rowNum) -> new AiImageAnalysisTask(rs.getLong("id"), rs.getString("request_id"),
                rs.getLong("media_asset_id"), rs.getString("media_url"), rs.getString("provider_code"),
                rs.getString("model_name"), rs.getString("status"), rs.getString("ingest_mode"),
                rs.getString("item_title"), rs.getString("summary"), rs.getString("experience"),
                list(rs.getString("tags_json")), rs.getString("decision"), longColumn(rs, "matched_category_id"),
                rs.getString("matched_category_name"), rs.getString("new_category_name"), rs.getString("new_category_slug"),
                rs.getString("new_category_description"), rs.getBigDecimal("confidence"), rs.getString("reason"),
                rs.getString("review_reason"), longColumn(rs, "created_category_id"), longColumn(rs, "created_item_id"),
                time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<AiCallLog> callLogMapper() {
        return (rs, rowNum) -> new AiCallLog(rs.getLong("id"), rs.getString("request_id"),
                rs.getString("provider_code"), rs.getString("model_name"), rs.getString("scenario"),
                rs.getString("status"), rs.getInt("prompt_tokens"), rs.getInt("completion_tokens"),
                rs.getInt("total_tokens"), rs.getBigDecimal("estimated_cost"), rs.getInt("duration_ms"),
                rs.getString("error_message"), longColumn(rs, "task_id"), time(rs.getObject("created_at")));
    }

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception e) {
            throw new ApiException(500, "AI 数据序列化失败");
        }
    }

    private List<String> list(String value) {
        try {
            if (value == null || value.isBlank()) {
                return List.of();
            }
            return mapper.readValue(value, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
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

    private BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return value == null || String.valueOf(value).isBlank() ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Long longColumn(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime time(Object value) {
        return value instanceof LocalDateTime localDateTime ? localDateTime : null;
    }

    private boolean bool(Boolean value, Boolean fallback) {
        return value == null ? Boolean.TRUE.equals(fallback) : value;
    }

    private int integer(Integer value, Integer fallback) {
        return value == null ? (fallback == null ? 0 : fallback) : value;
    }

    private BigDecimal decimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String trimSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
