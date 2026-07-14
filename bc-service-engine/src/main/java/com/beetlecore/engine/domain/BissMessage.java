package com.beetlecore.engine.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record BissMessage(Header header, Context context, Audit audit, JsonNode body) {

    public record Header(
            String token,
            String transactionCode,
            String system,
            boolean isSandbox,
            String openBankingConsentId,
            String authorizationToken
    ) {
    }

    public record Context(
            String tenantId,
            String branchId,
            String bookId,
            String systemLocale,
            String systemTimezone,
            String systemCurrency,
            Geolocation geolocation,
            String dataJurisdiction
    ) {
    }

    public record Geolocation(
            Double latitude,
            Double longitude,
            String country,
            String region
    ) {
    }

    public record Audit(
            String operatorId,
            String clientSessionId
    ) {
    }
}
