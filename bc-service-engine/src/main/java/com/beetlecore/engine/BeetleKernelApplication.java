package com.beetlecore.engine;

import com.beetlecore.engine.domain.BissMessage;
import com.beetlecore.engine.infrastructure.ApplicationConfig;
import com.beetlecore.engine.infrastructure.BeetleCoreRouter;
import com.beetlecore.engine.infrastructure.DataSourceRegistry;
import com.beetlecore.engine.infrastructure.DynamicPolicyInterceptor;
import com.beetlecore.engine.infrastructure.EntityAtomRepository;
import com.beetlecore.engine.infrastructure.SqlDataAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;

public final class BeetleKernelApplication {

    public static void main(String[] args) {
        int port = ApplicationConfig.getInt("beetlecore.server.port", 8080);
        DataSourceRegistry datasourceRegistry = new DataSourceRegistry();
        SqlDataAccessor accessor = new SqlDataAccessor(datasourceRegistry);
        BeetleCoreRouter router = new BeetleCoreRouter(
                accessor,
                new EntityAtomRepository(accessor),
                List.of(new DynamicPolicyInterceptor())
        );
        ObjectMapper mapper = new ObjectMapper();

        WebServer server = WebServer.create(builder -> builder.port(port).routing(routingBuilder -> routingBuilder.post("/api/v1/biss/stream", (request, response) -> {
            try {
                String body = request.content().as(String.class);
                handleRequest(body, response, router, mapper);
            } catch (Exception ex) {
                response.status(500).send("Unable to read request body: " + ex.getMessage());
            }
        })));

        server.start();
        System.out.println("BeetleKernelApplication started on port " + port);
    }

    private static void handleRequest(String body, ServerResponse response, BeetleCoreRouter router, ObjectMapper mapper) {
        try {
            BissMessage message = mapper.readValue(body, BissMessage.class);
            String jwtToken = message.header().authorizationToken();
            if (jwtToken == null || jwtToken.isBlank()) {
                response.status(400).send("Missing authorizationToken in BISS header.");
                return;
            }
            router.route(jwtToken, message);
            response.status(202).send("BISS event accepted");
        } catch (Exception e) {
            response.status(500).send("Unable to process BISS stream: " + e.getMessage());
        }
    }
}
