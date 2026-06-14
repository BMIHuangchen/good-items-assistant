package com.gooditems.service;

import com.gooditems.config.AppProperties;
import com.gooditems.exception.ApiException;
import com.gooditems.model.MediaAsset;
import com.gooditems.repository.AiRepository;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaStorageService {
    private final AppProperties properties;
    private final AiRepository aiRepository;

    public MediaStorageService(AppProperties properties, AiRepository aiRepository) {
        this.properties = properties;
        this.aiRepository = aiRepository;
    }

    public MediaAsset saveMiniAiUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "请先选择要分析的图片");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new ApiException(400, "只能上传图片文件");
        }
        ensureCosConfigured();
        String extension = extension(file.getOriginalFilename(), contentType);
        String objectKey = "good-items/ai-uploads/%s/%s%s".formatted(LocalDate.now(), UUID.randomUUID(), extension);
        try {
            byte[] bytes = file.getBytes();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType(contentType);
            COSClient client = cosClient();
            try (InputStream input = file.getInputStream()) {
                client.putObject(properties.getCos().getBucket(), objectKey, input, metadata);
            } finally {
                client.shutdown();
            }
            String publicUrl = trimSlash(properties.getCos().getBaseUrl()) + "/" + objectKey;
            return aiRepository.createMedia("MINI_AI_UPLOAD", file.getOriginalFilename(), contentType,
                    bytes.length, sha256(bytes), objectKey, publicUrl);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "图片上传到 COS 失败，请检查 COS 密钥和桶权限");
        }
    }

    private COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(properties.getCos().getSecretId(), properties.getCos().getSecretKey());
        return new COSClient(credentials, new ClientConfig(new Region(properties.getCos().getRegion())));
    }

    private void ensureCosConfigured() {
        if (blank(properties.getCos().getSecretId()) || blank(properties.getCos().getSecretKey())
                || blank(properties.getCos().getBucket()) || blank(properties.getCos().getRegion())) {
            throw new ApiException(500, "COS 上传密钥未配置，请在服务器环境变量中配置 COS_SECRET_ID 和 COS_SECRET_KEY");
        }
    }

    private String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private String extension(String filename, String contentType) {
        if (filename != null && filename.lastIndexOf('.') >= 0) {
            String value = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (value.length() <= 8) {
                return value;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String trimSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
