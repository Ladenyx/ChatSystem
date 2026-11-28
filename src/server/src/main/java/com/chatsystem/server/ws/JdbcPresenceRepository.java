package com.chatsystem.server.ws;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPresenceRepository implements PresenceRepository {
    private static final String ELIGIBLE_CONTACTS_SQL = """
            SELECT contacts.candidate_id
            FROM (
                SELECT CASE WHEN user_low_id = ? THEN user_high_id ELSE user_low_id END AS candidate_id
                FROM friendships
                WHERE user_low_id = ? OR user_high_id = ?
                UNION
                SELECT other.user_id AS candidate_id
                FROM conversations c
                JOIN conversation_members self_m
                    ON self_m.conversation_id = c.id
                   AND self_m.user_id = ?
                   AND self_m.left_at IS NULL
                JOIN conversation_members other
                    ON other.conversation_id = c.id
                   AND other.user_id <> ?
                   AND other.left_at IS NULL
                WHERE c.type = 'DIRECT'
            ) contacts
            JOIN users u ON u.id = contacts.candidate_id
            WHERE u.status = 'ACTIVE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM blocks b
                  WHERE (b.blocker_id = ? AND b.blocked_id = contacts.candidate_id)
                     OR (b.blocker_id = contacts.candidate_id AND b.blocked_id = ?)
              )
            ORDER BY contacts.candidate_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPresenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public List<UUID> findEligibleContactIds(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        return jdbcTemplate.queryForList(
                ELIGIBLE_CONTACTS_SQL,
                UUID.class,
                userId,
                userId,
                userId,
                userId,
                userId,
                userId,
                userId
        );
    }
}
