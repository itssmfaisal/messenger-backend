package com.backend.messenger.ws;

import com.backend.messenger.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class UserInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public UserInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null && !jwtUtil.isTokenExpired(token)) {
                    accessor.setUser(new StompPrincipal(username));
                } else {
                    throw new MessageDeliveryException("Invalid or expired token");
                }
            } catch (MessageDeliveryException e) {
                throw e;
            } catch (Exception e) {
                throw new MessageDeliveryException("Invalid token: " + e.getMessage());
            }
        }

        return message;
    }
}
