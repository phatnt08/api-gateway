package com.ntp.api_gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.ntp.api_gateway.repository.IdentityClient;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebClientConfiguration {

    @NonFinal
    @Value("${services-host.identity-service}")
    protected String IDENTITY_SERVICE_HOST;

    @Bean
    WebClient webClient() {
        return WebClient.builder().baseUrl(IDENTITY_SERVICE_HOST).build();
    }

    @Bean
    IdentityClient identityClient(WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }

}
