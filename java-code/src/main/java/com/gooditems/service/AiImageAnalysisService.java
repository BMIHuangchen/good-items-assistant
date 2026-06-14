package com.gooditems.service;

import com.gooditems.dto.AiConfirmTaskRequest;
import com.gooditems.dto.AiImageAnalysisResponse;
import com.gooditems.dto.AiMiniSettingsResponse;
import com.gooditems.dto.AiProviderAnalysis;
import com.gooditems.dto.CategoryRequest;
import com.gooditems.dto.GoodItemRequest;
import com.gooditems.exception.ApiException;
import com.gooditems.model.AiFeatureSettings;
import com.gooditems.model.AiImageAnalysisTask;
import com.gooditems.model.AiModelConfig;
import com.gooditems.model.Category;
import com.gooditems.model.GoodItem;
import com.gooditems.model.MediaAsset;
import com.gooditems.model.UserComputeQuota;
import com.gooditems.repository.AiRepository;
import com.gooditems.repository.ContentRepository;
import com.gooditems.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiImageAnalysisService {
    private static final String SCENARIO = "IMAGE_CLASSIFY";
    private final AiRepository aiRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final MediaStorageService mediaStorageService;
    private final AiProviderClient providerClient;

    public AiImageAnalysisService(AiRepository aiRepository, ContentRepository contentRepository,
                                  UserRepository userRepository, MediaStorageService mediaStorageService,
                                  AiProviderClient providerClient) {
        this.aiRepository = aiRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.mediaStorageService = mediaStorageService;
        this.providerClient = providerClient;
    }

    public AiMiniSettingsResponse miniSettings() {
        AiFeatureSettings settings = aiRepository.settings();
        List<AiMiniSettingsResponse.ModelOption> models = aiRepository.models(true).stream()
                .map(model -> new AiMiniSettingsResponse.ModelOption(model.providerCode(), model.displayName()))
                .toList();
        return new AiMiniSettingsResponse(settings.aiEnabled(), settings.autoIngestEnabled(),
                settings.autoPublishEnabled(), settings.maxImageSizeMb(), models);
    }

    public AiImageAnalysisResponse analyze(Long userId, String providerCode, MultipartFile file, String requestId) {
        AiFeatureSettings settings = aiRepository.settings();
        if (!Boolean.TRUE.equals(settings.aiEnabled())) {
            throw new ApiException(403, "AI 图片分析入口暂未开启");
        }
        if (file.getSize() > settings.maxImageSizeMb() * 1024L * 1024L) {
            throw new ApiException(400, "图片不能超过 %dMB".formatted(settings.maxImageSizeMb()));
        }
        if (aiRepository.todayCallCount() >= settings.dailyCallLimit()) {
            throw new ApiException(429, "今日 AI 调用次数已达上限，请明天再试");
        }
        ensureUserQuota(userId);
        AiModelConfig model = aiRepository.model(providerCode);
        if (!Boolean.TRUE.equals(model.enabled())) {
            throw new ApiException(403, "当前 AI 模型未启用");
        }
        byte[] imageBytes = bytes(file);
        MediaAsset media = mediaStorageService.saveMiniAiUpload(file);
        long started = System.currentTimeMillis();
        AiImageAnalysisTask task = null;
        try {
            AiProviderAnalysis analysis = providerClient.analyzeImage(model, media, imageBytes,
                    aiRepository.prompt(SCENARIO), contentRepository.publicCategories());
            Map<String, Object> result = resultMap(analysis);
            String reviewReason = reviewReason(settings, analysis);
            task = aiRepository.createTask(requestId, userId, media.id(), model.providerCode(), model.modelName(),
                    "PENDING_USER_CONFIRM", "USER", result, reviewReason);
            aiRepository.createCallLog(requestId, userId, model.providerCode(), model.modelName(), SCENARIO, "SUCCESS",
                    analysis.promptTokens(), analysis.completionTokens(), analysis.totalTokens(),
                    cost(model, analysis.promptTokens(), analysis.completionTokens()),
                    elapsed(started), null, task.id());
            return response(task);
        } catch (ApiException e) {
            aiRepository.createCallLog(requestId, userId, model.providerCode(), model.modelName(), SCENARIO, "FAILED",
                    0, 0, 0, BigDecimal.ZERO, elapsed(started), e.getMessage(), task == null ? null : task.id());
            throw e;
        } catch (Exception e) {
            aiRepository.createCallLog(requestId, userId, model.providerCode(), model.modelName(), SCENARIO, "FAILED",
                    0, 0, 0, BigDecimal.ZERO, elapsed(started), e.getMessage(), task == null ? null : task.id());
            throw new ApiException(502, "AI 图片分析失败，请稍后再试");
        }
    }

    public AiImageAnalysisTask confirm(Long taskId, AiConfirmTaskRequest request) {
        return confirm(taskId, request, null);
    }

    public AiImageAnalysisTask confirm(Long taskId, AiConfirmTaskRequest request, Long userId) {
        AiImageAnalysisTask task = aiRepository.task(taskId);
        if (userId != null && !userId.equals(task.userId())) {
            throw new ApiException(403, "只能确认自己的 AI 分析结果");
        }
        if (!List.of("PENDING_REVIEW", "ANALYZED", "PENDING_USER_CONFIRM").contains(task.status())) {
            throw new ApiException(400, "当前任务状态不能确认入库");
        }
        Long categoryId = resolveCategoryId(task, request);
        GoodItem item = contentRepository.createItem(new GoodItemRequest(
                categoryId,
                request.itemTitle(),
                request.summary(),
                request.experience(),
                request.tags(),
                task.mediaUrl(),
                List.of(task.mediaUrl()),
                Boolean.TRUE.equals(request.publish()) ? "PUBLISHED" : "DRAFT",
                0
        ));
        aiRepository.markTaskIngested(taskId, userId == null ? "CONFIRMED" : "USER_CONFIRMED", userId == null ? "MANUAL" : "USER",
                Boolean.TRUE.equals(request.createNewCategory()) ? categoryId : null, item.id());
        return aiRepository.task(taskId);
    }

    public void reject(Long taskId, String reason) {
        aiRepository.rejectTask(taskId, reason);
    }

    public AiImageAnalysisResponse response(AiImageAnalysisTask task) {
        return new AiImageAnalysisResponse(task.id(), task.status(), task.ingestMode(), task.userId(), task.mediaUrl(),
                task.providerCode(), task.modelName(), task.itemTitle(), task.summary(), task.experience(),
                task.tags(), task.decision(), task.matchedCategoryId(), task.matchedCategoryName(),
                task.newCategoryName(), task.confidence(), task.reason(), task.createdItemId(),
                task.createdCategoryId(), task.reviewReason());
    }

    private AiImageAnalysisTask autoIngest(AiImageAnalysisTask task, AiFeatureSettings settings) {
        Long categoryId = resolveCategoryId(task, new AiConfirmTaskRequest(
                task.matchedCategoryId(), "NEW_CATEGORY".equals(task.decision()), task.newCategoryName(),
                task.newCategorySlug(), task.newCategoryDescription(), task.itemTitle(), task.summary(),
                task.experience(), task.tags(), settings.autoPublishEnabled()
        ));
        GoodItem item = contentRepository.createItem(new GoodItemRequest(
                categoryId,
                fallback(task.itemTitle(), "AI 识别好物"),
                task.summary(),
                task.experience(),
                task.tags(),
                task.mediaUrl(),
                List.of(task.mediaUrl()),
                Boolean.TRUE.equals(settings.autoPublishEnabled()) ? "PUBLISHED" : "DRAFT",
                0
        ));
        aiRepository.markTaskIngested(task.id(), "AUTO_INGESTED", "AUTO",
                "NEW_CATEGORY".equals(task.decision()) ? categoryId : null, item.id());
        return aiRepository.task(task.id());
    }

    private Long resolveCategoryId(AiImageAnalysisTask task, AiConfirmTaskRequest request) {
        if (Boolean.TRUE.equals(request.createNewCategory())) {
            Category category = contentRepository.createCategory(new CategoryRequest(
                    fallback(request.newCategoryName(), task.newCategoryName()),
                    uniqueSlug(fallback(request.newCategorySlug(), task.newCategorySlug())),
                    fallback(request.newCategoryDescription(), task.newCategoryDescription()),
                    task.mediaUrl(),
                    0,
                    true
            ));
            return category.id();
        }
        Long categoryId = request.categoryId() != null ? request.categoryId() : task.matchedCategoryId();
        if (categoryId == null) {
            throw new ApiException(400, "请选择已有分类或填写新分类");
        }
        contentRepository.findCategory(categoryId);
        return categoryId;
    }

    private String reviewReason(AiFeatureSettings settings, AiProviderAnalysis analysis) {
        if (analysis.itemTitle() == null || analysis.itemTitle().isBlank()) {
            return "AI 未识别出明确物品标题";
        }
        if (!List.of("EXISTING_CATEGORY", "NEW_CATEGORY").contains(analysis.decision())) {
            return "AI 分类决策不合法";
        }
        if ("EXISTING_CATEGORY".equals(analysis.decision()) && analysis.categoryId() == null) {
            return "AI 未返回已有分类 ID";
        }
        if ("NEW_CATEGORY".equals(analysis.decision()) && (analysis.newCategoryName() == null || analysis.newCategoryName().isBlank())) {
            return "AI 未返回新分类名称";
        }
        if (Boolean.TRUE.equals(settings.lowConfidenceReviewEnabled())
                && analysis.confidence().compareTo(settings.confidenceThreshold()) < 0) {
            return "AI 置信度低于自动入库阈值";
        }
        if ("EXISTING_CATEGORY".equals(analysis.decision())) {
            try {
                contentRepository.findCategory(analysis.categoryId());
            } catch (ApiException e) {
                return "AI 返回的分类不存在";
            }
        }
        return null;
    }

    private Map<String, Object> resultMap(AiProviderAnalysis analysis) {
        Map<String, Object> result = new HashMap<>();
        result.put("itemTitle", analysis.itemTitle());
        result.put("summary", analysis.summary());
        result.put("experience", analysis.experience());
        result.put("tags", analysis.tags());
        result.put("decision", analysis.decision());
        result.put("categoryId", analysis.categoryId());
        result.put("newCategoryName", analysis.newCategoryName());
        result.put("newCategorySlug", analysis.newCategorySlug());
        result.put("newCategoryDescription", analysis.newCategoryDescription());
        result.put("confidence", analysis.confidence());
        result.put("reason", analysis.reason());
        result.put("rawJson", analysis.rawJson());
        return result;
    }

    private BigDecimal cost(AiModelConfig model, int promptTokens, int completionTokens) {
        BigDecimal prompt = model.promptPricePer1k().multiply(BigDecimal.valueOf(promptTokens)).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        BigDecimal completion = model.completionPricePer1k().multiply(BigDecimal.valueOf(completionTokens)).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        return prompt.add(completion);
    }

    private void ensureUserQuota(Long userId) {
        UserComputeQuota quota = userRepository.userComputeQuota(userId);
        if (quota.dailyCallLimit() != null && quota.dailyCallLimit() > 0 && quota.todayCalls() >= quota.dailyCallLimit()) {
            throw new ApiException(429, "今日 AI 调用次数已达到会员上限，请明天再试");
        }
        if (quota.dailyTokenLimit() != null && quota.dailyTokenLimit() > 0 && quota.todayTokens() >= quota.dailyTokenLimit()) {
            throw new ApiException(429, "今日 AI 算力额度已用完，请明天再试");
        }
        if (quota.monthlyTokenLimit() != null && quota.monthlyTokenLimit() > 0 && quota.monthTokens() >= quota.monthlyTokenLimit()) {
            throw new ApiException(429, "本月 AI 算力额度已用完，请下月再试");
        }
    }

    private byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new ApiException(400, "读取上传图片失败");
        }
    }

    private int elapsed(long started) {
        return Math.toIntExact(Math.max(0, System.currentTimeMillis() - started));
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String uniqueSlug(String value) {
        String base = value == null || value.isBlank() ? "ai-category" : value;
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFKD)
                .replaceAll("[^a-zA-Z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            normalized = "ai-category";
        }
        return normalized + "-" + System.currentTimeMillis();
    }
}
