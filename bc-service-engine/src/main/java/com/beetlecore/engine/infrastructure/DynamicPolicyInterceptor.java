package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissInterceptor;
import com.beetlecore.engine.domain.BissMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.config.Config;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class DynamicPolicyInterceptor implements BissInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String engineEndpoint;
    private final JsonNode localPolicyDocument;

    public DynamicPolicyInterceptor() {
        this.engineEndpoint = ApplicationConfig.getString("beetlecore.policy.engine.endpoint", "").trim();
        this.localPolicyDocument = loadLocalPolicyDocument();
    }

    @Override
    public boolean preExecute(BissMessage message) throws SecurityException {
        if (!engineEndpoint.isBlank()) {
            return evaluateExternalPolicy(message);
        }

        if (localPolicyDocument != null) {
            return evaluateLocalPolicy(localPolicyDocument, message);
        }

        return true;
    }

    private JsonNode loadLocalPolicyDocument() {
        Config policyConfig = ApplicationConfig.config().get("beetlecore.policy.document");
        if (!policyConfig.exists()) {
            return null;
        }

        try {
            return policyConfig.as(String.class)
                    .map(this::parseJson)
                    .orElseGet(() -> MAPPER.valueToTree(policyConfig.asMap().orElse(Collections.emptyMap())));
        } catch (Exception e) {
            throw new SecurityException("Unable to parse local policy document", e);
        }
    }

    private boolean evaluateExternalPolicy(BissMessage message) throws SecurityException {
        try {
            URI uri = URI.create(engineEndpoint);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JsonNode requestPayload = MAPPER.createObjectNode().set("contract", toContractJson(message));
            byte[] payloadBytes = MAPPER.writeValueAsBytes(requestPayload);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payloadBytes);
            }

            int status = connection.getResponseCode();
            InputStream responseStream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            JsonNode response = parseJson(new String(responseStream.readAllBytes(), StandardCharsets.UTF_8));

            if (response.path("allow").isBoolean()) {
                return response.path("allow").asBoolean();
            }
            if (response.path("result").path("allow").isBoolean()) {
                return response.path("result").path("allow").asBoolean();
            }

            throw new SecurityException("Invalid policy response from engine endpoint");
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new SecurityException("Policy engine evaluation failed", e);
        }
    }

    private boolean evaluateLocalPolicy(JsonNode policy, BissMessage message) {
        JsonNode contract = toContractJson(message);

        if (policy.has("deny") && policy.path("deny").asBoolean(false)) {
            return false;
        }
        if (policy.has("allow")) {
            return policy.path("allow").asBoolean();
        }
        if (policy.has("rules") && policy.path("rules").isArray()) {
            for (JsonNode rule : policy.path("rules")) {
                JsonNode when = rule.path("when");
                if (matchesConditions(when, contract)) {
                    JsonNode then = rule.path("then");
                    if (then.has("deny") && then.path("deny").asBoolean(false)) {
                        return false;
                    }
                    if (then.has("allow")) {
                        return then.path("allow").asBoolean();
                    }
                }
            }
        }
        if (policy.has("conditions") && matchesConditions(policy.path("conditions"), contract)) {
            return policy.path("allow").asBoolean(true);
        }
        return true;
    }

    private boolean matchesConditions(JsonNode conditions, JsonNode contract) {
        if (!conditions.isObject()) {
            return false;
        }

        for (String fieldName : iterable(conditions.fieldNames())) {
            JsonNode expected = conditions.get(fieldName);
            JsonNode actual = resolvePath(contract, fieldName);
            if (!matchesValue(expected, actual)) {
                return false;
            }
        }
        return true;
    }

    private JsonNode resolvePath(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private boolean matchesValue(JsonNode expected, JsonNode actual) {
        if (expected == null || expected.isNull()) {
            return actual == null || actual.isNull();
        }
        if (actual == null || actual.isNull()) {
            return false;
        }
        if (expected.isObject()) {
            if (expected.has("equals")) {
                return actual.asText().equals(expected.path("equals").asText());
            }
            if (expected.has("in") && expected.path("in").isArray()) {
                for (JsonNode candidate : expected.path("in")) {
                    if (actual.asText().equals(candidate.asText())) {
                        return true;
                    }
                }
                return false;
            }
            if (expected.has("not")) {
                return !matchesValue(expected.path("not"), actual);
            }
        }
        if (expected.isArray()) {
            for (JsonNode candidate : expected) {
                if (matchesValue(candidate, actual)) {
                    return true;
                }
            }
            return false;
        }
        return expected.asText().equals(actual.asText());
    }

    private JsonNode toContractJson(BissMessage message) {
        return MAPPER.valueToTree(message);
    }

    private JsonNode parseJson(String payload) {
        try {
            return MAPPER.readTree(payload);
        } catch (Exception e) {
            throw new SecurityException("Unable to parse policy JSON", e);
        }
    }

    private static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }
}
