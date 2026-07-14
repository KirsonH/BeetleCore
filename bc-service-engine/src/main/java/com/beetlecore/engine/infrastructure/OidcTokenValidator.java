package com.beetlecore.engine.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

public class OidcTokenValidator {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Decodifica y extrae los claims del JWT emitido por Keycloak sin introducir librerías pesadas.
     * En producción, esto validará la firma criptográfica contra el endpoint JWKS de Keycloak.
     */
    public JsonNode parseKeycloakToken(String jwtToken) {
        try {
            String[] chunks = jwtToken.split("\\.");
            if (chunks.length < 2) {
                throw new IllegalArgumentException("Invalid JWT Token format");
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return mapper.readTree(payload);
        } catch (Exception e) {
            throw new SecurityException("Failed to parse OIDC Federation Token: " + e.getMessage(), e);
        }
    }

    public String getFederatedIdentityId(String jwtToken) {
        JsonNode claims = parseKeycloakToken(jwtToken);
        if (claims.hasNonNull("sub")) {
            return claims.get("sub").asText();
        }
        throw new SecurityException("OIDC token does not contain a federated subject claim ('sub').");
    }
}
