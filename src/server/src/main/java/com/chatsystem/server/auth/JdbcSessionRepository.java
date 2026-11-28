package com.chatsystem.server.auth;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSessionRepository implements SessionRepository {
    private static final String SELECT_JOINED = """
            SELECT s.id AS session_id,
                   s.user_id,
                   u.username,
                   u.full_name,
                   u.email,
                   u.role,
                   u.status,
                   u.email_verified_at,
                   s.scope,
                   s.created_at,
                   s.revoked_at
            FROM user_sessions s
            JOIN users u ON u.id = s.user_id
            """;

    private static final RowMapper<AuthenticatedSession> SESSION_MAPPER = JdbcSessionRepository::mapSession;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public Optional<AuthenticatedSession> findByTokenHash(String tokenHash) {
        Objects.requireNonNull(tokenHash, "tokenHash");
        List<AuthenticatedSession> rows = jdbcTemplate.query(
                SELECT_JOINED + " WHERE s.token_hash = ?",
                SESSION_MAPPER,
                tokenHash
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<AuthenticatedSession> findBySessionId(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        List<AuthenticatedSession> rows = jdbcTemplate.query(
                SELECT_JOINED + " WHERE s.id = ?",
                SESSION_MAPPER,
                sessionId
        );
        return rows.stream().findFirst();
    }

    @Override
    public boolean revokeSession(UUID sessionId, Instant revokedAt) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(revokedAt, "revokedAt");
        int updated = jdbcTemplate.update(
                "UPDATE user_sessions SET revoked_at = ? WHERE id = ? AND revoked_at IS NULL",
                Timestamp.from(revokedAt),
                sessionId
        );
        return updated > 0;
    }

    @Override
    public List<UUID> revokeAllUserSessions(UUID userId, Instant revokedAt, UUID excludedSessionId) {
        return revokeMatchingSessions(userId, revokedAt, excludedSessionId, false);
    }

    @Override
    public List<UUID> revokeActiveAdminSessions(UUID userId, Instant revokedAt, UUID excludedSessionId) {
        return revokeMatchingSessions(userId, revokedAt, excludedSessionId, true);
    }

    private List<UUID> revokeMatchingSessions(
            UUID userId,
            Instant revokedAt,
            UUID excludedSessionId,
            boolean adminOnly
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(revokedAt, "revokedAt");
        Timestamp revokedTs = Timestamp.from(revokedAt);
        String scopeClause = adminOnly ? " AND scope = 'ADMIN_APP'" : "";
        if (excludedSessionId == null) {
            return jdbcTemplate.queryForList(
                    "UPDATE user_sessions SET revoked_at = ? WHERE user_id = ?" + scopeClause
                            + " AND revoked_at IS NULL RETURNING id",
                    UUID.class,
                    revokedTs,
                    userId
            );
        }
        return jdbcTemplate.queryForList(
                "UPDATE user_sessions SET revoked_at = ? WHERE user_id = ?" + scopeClause
                        + " AND revoked_at IS NULL AND id <> ? RETURNING id",
                UUID.class,
                revokedTs,
                userId,
                excludedSessionId
        );
    }

    private static AuthenticatedSession mapSession(ResultSet rs, int rowNum) throws SQLException {
        Timestamp revokedTs = rs.getTimestamp("revoked_at");
        return new AuthenticatedSession(
                rs.getObject("session_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                UserRole.valueOf(rs.getString("role")),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("email_verified_at") != null,
                SessionScope.valueOf(rs.getString("scope")),
                rs.getTimestamp("created_at").toInstant(),
                revokedTs == null ? null : revokedTs.toInstant(),
                null
        );
    }
}
