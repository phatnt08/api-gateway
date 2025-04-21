package com.ntp.api_gateway.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntp.api_gateway.dto.response.ApiResponse;
import com.ntp.api_gateway.service.IdentityService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j // Lombok annotation for logging
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/**
 * AuthenticationFulter is a global filter for handling authentication in the
 * API Gateway.
 * It implements the GlobalFilter interface and is annotated with @Component to
 * be managed by Spring.
 * The filter method is called for each request, allowing for custom
 * authentication logic to be applied.
 */
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;

    ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Authentication filter executed for request: {}", exchange.getRequest().getURI());

        // Get token from request header
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || token.isEmpty()) {
            return getUnAuthenticatedResponse(exchange);
        }

        token = token.replace("Bearer ", "");

        // verify token at identity sevice
        return identityService.introspect(token).flatMap(res -> {
            log.info("result {}", res.getResult().isValid());
            if (res.getResult().isValid()) {
                return chain.filter(exchange);
            } else {
                return getUnAuthenticatedResponse(exchange);
            }
        }).onErrorResume(_ -> getUnAuthenticatedResponse(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Set the order of this filter
    }

    private Mono<Void> getUnAuthenticatedResponse(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        String body = null;

        ApiResponse<?> apiResponse = ApiResponse.builder().code(HttpStatus.UNAUTHORIZED.value())
                .message(HttpStatus.UNAUTHORIZED.getReasonPhrase()).build();

        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
