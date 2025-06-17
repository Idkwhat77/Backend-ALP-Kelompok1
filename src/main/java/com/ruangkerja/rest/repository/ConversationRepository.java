package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Find conversation between two users
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1.id = :user1Id AND c.user2.id = :user2Id) OR " +
           "(c.user1.id = :user2Id AND c.user2.id = :user1Id)")
    Optional<Conversation> findConversationBetweenUsers(@Param("user1Id") Long user1Id, 
                                                       @Param("user2Id") Long user2Id);

    // Find all conversations for a user
    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findConversationsForUser(@Param("userId") Long userId);

    // Search conversations by user name
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1.id = :userId AND c.user2.email LIKE %:searchTerm%) OR " +
           "(c.user2.id = :userId AND c.user1.email LIKE %:searchTerm%) " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> searchConversationsForUser(@Param("userId") Long userId, 
                                                 @Param("searchTerm") String searchTerm);
}