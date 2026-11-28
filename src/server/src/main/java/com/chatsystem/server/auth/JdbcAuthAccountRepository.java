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
public class JdbcAuthAccountRepository implements AuthAccountRepository {
    private static final RowMapper<AuthAccountRecord> ACCOUNT_MAPPER = JdbcAuthAccountRepository::mapAccount;

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public Optional<AuthAccountRecord> findByUsernameIgnoreCase(String username) {
        Objects.requireNonNull(username, "username");
        List<AuthAccountRecord> rows = jdbcTemplate.query(
                """
                SELECT id, username, full_name, email, role, status, password_hash, email_verified_at
                FROM users
                WHERE lower(username) = lower(?)
                """,
                ACCOUNT_MAPPER,
                username
        );
        return rows.stream().findFirst();
    }

    @Override
    public UUID insertSession(UUID userId, SessionScope scope, String tokenHash, Instant createdAt) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(createdAt, "createdAt");
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO user_sessions (user_id, scope, token_hash, created_at)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                UUID.class,
                userId,
                scope.name(),
                tokenHash,
                Timestamp.from(createdAt)
        );
    }

    @Override
    public void insertLoginLog(UUID userId, SessionScope scope, Instant loginAt) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(loginAt, "loginAt");
        jdbcTemplate.update(
                "INSERT INTO public.login_logs (user_id, session_scope, login_at) VALUES (?, ?, ?)",
                userId,
                scope.name(),
                Timestamp.from(loginAt)
        );
    }

    @Override
    public void recordAppOpen(UUID userId, UUID launchId, SessionScope scope) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(launchId, "launchId");
        Objects.requireNonNull(scope, "scope");
        jdbcTemplate.queryForObject(
                "SELECT public.record_app_open(?, ?, ?)",
                UUID.class,
                userId,
                launchId,
                scope.name()
        );
    }

    private static AuthAccountRecord mapAccount(ResultSet rs, int rowNum) throws SQLException {
        return new AuthAccountRecord(
                rs.getObject("id", UUID.class),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                UserRole.valueOf(rs.getString("role")),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getString("password_hash"),
                rs.getTimestamp("email_verified_at") != null
        );
    }
}
