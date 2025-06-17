package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.ChatMessageWebSocketDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessageWebSocketDto message, Principal principal) {
        // Add logging to debug
        System.out.println("Received typing indicator from: " + message.getSenderId() + " to: " + message.getReceiverId());
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        // Broadcast typing indicator to receiver using the receiver's user ID as the principal name
        message.setType("typing");
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/typing",
            message
        );
        
        System.out.println("Sent typing indicator to user: " + message.getReceiverId());
    }

    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(@Payload ChatMessageWebSocketDto message, Principal principal) {
        System.out.println("Received stop typing indicator from: " + message.getSenderId() + " to: " + message.getReceiverId());
        
        // Broadcast stop typing indicator to receiver
        message.setType("stopTyping");
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/typing",
            message
        );
        
        System.out.println("Sent stop typing indicator to user: " + message.getReceiverId());
    }
}