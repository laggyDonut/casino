package de.edvschuleplattling.irgendwieanders.config;

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
        // Hier verbinden sich deine Clients (z.B. Angular/React) anfangs
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Im echten Projekt auf deine Frontend-URL einschränken!
                .withSockJS(); // Fallback für ältere Browser
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" nutzt du, wenn du an ALLE am Tisch senden willst (z.B. "Karten werden ausgeteilt")
        // "/queue" nutzt du für private Nachrichten an einen einzelnen User (z.B. "Deine Handkarten")
        config.enableSimpleBroker("/topic", "/queue");

        // Präfix für Nachrichten, die VOM Client ZUM Server gehen
        config.setApplicationDestinationPrefixes("/app");
    }
}