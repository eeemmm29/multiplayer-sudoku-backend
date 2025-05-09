package com.em.multiplayersudoku;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // messages to destinations prefixed with /app routed to @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // simple in-memory broker for /topic broadcasts
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // clients connect here (with SockJS fallback if desired)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}