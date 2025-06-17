package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user1_id", "user2_id"}, 
           name = "uk_conversation_users"
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User1 is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_user1"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user1;

    @NotNull(message = "User2 is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conversation_user2"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id", foreignKey = @ForeignKey(name = "fk_conversation_last_message"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Message lastMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}