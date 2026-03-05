package com.backend.messenger.repository;

import com.backend.messenger.model.Message;
import com.backend.messenger.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndRecipientOrRecipientAndSender(String sender1, String recipient1, String sender2, String recipient2);

    /** Find undelivered messages for a user (used to mark delivered on connect). */
    List<Message> findByRecipientAndStatus(String recipient, MessageStatus status);

    /**
     * Returns distinct usernames the given user has exchanged messages with,
     * along with the latest message timestamp, ordered newest-first.
     */
    @Query(value = """
        SELECT partner, MAX(sent_at) AS last_message_at
          FROM (
            SELECT CASE WHEN sender = :user THEN recipient ELSE sender END AS partner,
                   sent_at
              FROM message
             WHERE sender = :user OR recipient = :user
          ) sub
         GROUP BY partner
         ORDER BY last_message_at DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT CASE WHEN sender = :user THEN recipient ELSE sender END)
          FROM message
         WHERE sender = :user OR recipient = :user
    """,
    nativeQuery = true)
    Page<Object[]> findConversationPartners(@Param("user") String user, Pageable pageable);
}
