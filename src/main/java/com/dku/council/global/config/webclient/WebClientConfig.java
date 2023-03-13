package com.dku.council.global.config.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // TODO Docker에 올린 뒤에 UnknownHostException 발생 가능성
    @Bean
    @Primary
    public WebClient plainWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(connectionProvider())))
                .build();
    }

    @Bean
    @Qualifier("chromeAgentWebClient")
    public WebClient chromeAgentWebClient() {
        return WebClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .build();
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("app-connection-provider")
                .maxIdleTime(Duration.ofSeconds(3))
                .evictInBackground(Duration.ofSeconds(30))
                .lifo()
                .build();
    }
}
