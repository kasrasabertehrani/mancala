package com.mancalagame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the URL players will use to establish the initial connection
        registry.addEndpoint("/mancala-ws")
                .setAllowedOriginPatterns("*") // Allows your frontend to connect from a different port
                .withSockJS(); // A fallback just in case the browser doesn't support raw WebSockets
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // The "loudspeaker". The server broadcasts game updates to anything starting with "/topic"
        registry.enableSimpleBroker("/topic");

        // The "mailbox". Messages sent FROM the client to the server must start with "/app"
        registry.setApplicationDestinationPrefixes("/app");
    }
}