package de.wigeogis.pmedian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(final MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/");
  }

  @Override
  public void registerStompEndpoints(final StompEndpointRegistry registry) {

    String[] allowedOrigins =
        new String[] {
          "*://localhost:*",
          "*://127.0.0.1:*",
          "*://10.1.2.*:*",
          "*://192.168.*.*:*",
          "https://social-data.wigeogis.com/"
        };

    registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins);

    registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins).withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            assert accessor != null;
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
              MessageHeaders headers = message.getHeaders();
            }
            return message;
          }
        });
  }
}
