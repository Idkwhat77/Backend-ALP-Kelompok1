package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages between two users with pagination
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.createdAt ASC")
    Page<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, 
                                          @Param("user2Id") Long user2Id, 
                                          Pageable pageable);

    // Find latest message between two users
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessageBetweenUsers(@Param("user1Id") Long user1Id, 
                                               @Param("user2Id") Long user2Id, 
                                               Pageable pageable);

    // Count unread messages for a user from another user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId " +
           "AND m.sender.id = :senderId AND m.isRead = false")
    Long countUnreadMessages(@Param("receiverId") Long receiverId, 
                            @Param("senderId") Long senderId);

    // Mark messages as read
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver.id = :receiverId " +
           "AND m.sender.id = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("receiverId") Long receiverId, 
                           @Param("senderId") Long senderId);

    // Find all users who have conversations with a specific user
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver ELSE m.sender END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<com.ruangkerja.rest.entity.User> findConversationPartners(@Param("userId") Long userId);
}