package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.MessageDto;
import com.ruangkerja.rest.dto.ChatMessageWebSocketDto;
import com.ruangkerja.rest.entity.Conversation;
import com.ruangkerja.rest.entity.Message;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.Company;
import com.ruangkerja.rest.repository.ConversationRepository;
import com.ruangkerja.rest.repository.MessageRepository;
import com.ruangkerja.rest.repository.UserRepository;
import com.ruangkerja.rest.repository.CandidateRepository;
import com.ruangkerja.rest.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private CompanyRepository companyRepository;

    // Add WebSocket messaging template
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Get all conversations for a user with profile information
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserConversationsWithProfiles(@PathVariable Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsForUser(userId);
        
        List<Map<String, Object>> conversationsWithProfiles = conversations.stream()
            .map(conversation -> {
                Map<String, Object> convData = new HashMap<>();
                convData.put("id", conversation.getId());
                convData.put("createdAt", conversation.getCreatedAt());
                convData.put("updatedAt", conversation.getUpdatedAt());
                
                // Determine the other user in the conversation
                User otherUser = conversation.getUser1().getId().equals(userId) 
                    ? conversation.getUser2() 
                    : conversation.getUser1();
                
                // Get profile information for the other user
                Map<String, Object> otherUserProfile = getUserProfileData(otherUser);
                convData.put("otherUser", otherUserProfile);
                
                // Add last message if exists
                if (conversation.getLastMessage() != null) {
                    Map<String, Object> lastMsg = new HashMap<>();
                    lastMsg.put("content", conversation.getLastMessage().getContent());
                    lastMsg.put("createdAt", conversation.getLastMessage().getCreatedAt());
                    lastMsg.put("senderId", conversation.getLastMessage().getSender().getId());
                    convData.put("lastMessage", lastMsg);
                }
                
                return convData;
            })
            .toList();
        
        return ResponseEntity.ok(conversationsWithProfiles);
    }

    // Get messages between two users
    @GetMapping("/messages/{user1Id}/{user2Id}")
    public ResponseEntity<Page<Message>> getMessagesBetweenUsers(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findMessagesBetweenUsers(user1Id, user2Id, pageable);
        return ResponseEntity.ok(messages);
    }

    // Send a message
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageDto messageDto) {
        try {
            // Get sender and receiver
            Optional<User> sender = userRepository.findById(messageDto.getSender().getId());
            Optional<User> receiver = userRepository.findById(messageDto.getReceiver().getId());
            
            if (sender.isEmpty() || receiver.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Create message
            Message message = new Message();
            message.setSender(sender.get());
            message.setReceiver(receiver.get());
            message.setContent(messageDto.getContent());
            message.setIsRead(false);
            
            Message savedMessage = messageRepository.save(message);

            // Update or create conversation
            updateConversation(sender.get(), receiver.get(), savedMessage);

            // Send real-time notification via WebSocket
            sendRealTimeMessage(savedMessage, sender.get(), receiver.get());

            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void sendRealTimeMessage(Message message, User sender, User receiver) {
        // Get sender profile info
        Map<String, Object> senderProfile = getUserProfileData(sender);
        
        // Create WebSocket message
        ChatMessageWebSocketDto wsMessage = new ChatMessageWebSocketDto();
        wsMessage.setId(message.getId());
        wsMessage.setContent(message.getContent());
        wsMessage.setSenderId(sender.getId());
        wsMessage.setSenderName((String) senderProfile.get("fullName"));
        wsMessage.setSenderProfileImageUrl((String) senderProfile.get("profileImageUrl"));
        wsMessage.setReceiverId(receiver.getId());
        wsMessage.setCreatedAt(message.getCreatedAt());
        wsMessage.setType("message");

        System.out.println("Sending real-time message to user: " + receiver.getId());
        System.out.println("Message content: " + message.getContent());

        // Send to specific user using their user ID as the principal name
        messagingTemplate.convertAndSendToUser(
            receiver.getId().toString(), 
            "/queue/messages", 
            wsMessage
        );
        
        System.out.println("Message sent via WebSocket to user: " + receiver.getId());
    }

    // Mark messages as read
    @PutMapping("/mark-read/{senderId}/{receiverId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long senderId, 
            @PathVariable Long receiverId) {
        
        messageRepository.markMessagesAsRead(receiverId, senderId);
        return ResponseEntity.ok().build();
    }

    // Helper method to get user profile data (Candidate or Company)
    private Map<String, Object> getUserProfileData(User user) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("userId", user.getId());
        profileData.put("email", user.getEmail());
        
        // Try to find candidate profile first
        Optional<Candidate> candidate = candidateRepository.findByUserId(user.getId());
        if (candidate.isPresent()) {
            Candidate c = candidate.get();
            profileData.put("type", "candidate");
            profileData.put("fullName", c.getFullName());
            profileData.put("profileImageUrl", c.getProfileImageUrl());
            profileData.put("industry", c.getIndustry());
            profileData.put("city", c.getCity());
            return profileData;
        }
        
        // Try to find company profile
        Optional<Company> company = companyRepository.findByUserId(user.getId());
        if (company.isPresent()) {
            Company comp = company.get();
            profileData.put("type", "company");
            profileData.put("fullName", comp.getCompanyName());
            profileData.put("profileImageUrl", comp.getProfileImageUrl());
            profileData.put("industry", comp.getIndustry());
            profileData.put("hq", comp.getHq());
            return profileData;
        }
        
        // Fallback if no profile found
        profileData.put("type", "user");
        profileData.put("fullName", "Unknown User");
        profileData.put("profileImageUrl", null);
        
        return profileData;
    }

    private void updateConversation(User user1, User user2, Message lastMessage) {
        Optional<Conversation> existingConversation = 
            conversationRepository.findConversationBetweenUsers(user1.getId(), user2.getId());
        
        if (existingConversation.isPresent()) {
            Conversation conversation = existingConversation.get();
            conversation.setLastMessage(lastMessage);
            conversationRepository.save(conversation);
        } else {
            Conversation newConversation = new Conversation();
            newConversation.setUser1(user1);
            newConversation.setUser2(user2);
            newConversation.setLastMessage(lastMessage);
            conversationRepository.save(newConversation);
        }
    }
}