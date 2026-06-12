package com.gooditems.dto;

import java.util.List;

public record AiMiniSettingsResponse(
        Boolean aiEnabled,
        Boolean autoIngestEnabled,
        Boolean autoPublishEnabled,
        Integer maxImageSizeMb,
        List<ModelOption> models
) {
    public record ModelOption(String providerCode, String displayName) {
    }
}
