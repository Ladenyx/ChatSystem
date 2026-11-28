package com.chatsystem.server.admin;

import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAdminUserRepository implements AdminUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAdminUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public OffsetPage<AdminUserListItemDto> findUsers(AdminUserQuery query) {
        Objects.requireNonNull(query, "query");
        List<String> predicates = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (query.name() != null) {
            predicates.add("lower(full_name) LIKE ? ESCAPE '\\'");
            params.add("%" + escapeLike(query.name().toLowerCase(Locale.ROOT)) + "%");
        }
        if (query.username() != null) {
            predicates.add("lower(username) LIKE ? ESCAPE '\\'");
            params.add("%" + escapeLike(query.username().toLowerCase(Locale.ROOT)) + "%");
        }
        if (query.status() != null) {
            predicates.add("status = ?");
            params.add(query.status().name());
        }

        String whereClause = predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
        Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users" + whereClause,
                Long.class,
                params.toArray()
        );
        long totalItems = totalCount == null ? 0L : totalCount;

        List<Object> rowParams = new ArrayList<>(params);
        rowParams.add(query.size());
        rowParams.add((long) query.page() * query.size());

        List<AdminUserListItemDto> items = jdbcTemplate.query(
                "SELECT id, username, full_name, role, status, created_at FROM users"
                        + whereClause
                        + " ORDER BY " + orderClause(query.sortBy(), query.sortDir())
                        + " LIMIT ? OFFSET ?",
                JdbcAdminUserRepository::mapUserRow,
                rowParams.toArray()
        );
        return OffsetPage.of(items, query.page(), query.size(), totalItems);
    }

    private static String orderClause(AdminUserSortField sortBy, SortDirection sortDir) {
        String direction = sortDir == SortDirection.DESC ? "DESC" : "ASC";
        return switch (sortBy) {
            case FULL_NAME -> "lower(full_name) " + direction + ", id ASC";
            case CREATED_AT -> "created_at " + direction + ", id ASC";
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static AdminUserListItemDto mapUserRow(ResultSet rs, int rowNum) throws SQLException {
        return new AdminUserListItemDto(
                rs.getObject("id", UUID.class),
                rs.getString("username"),
                rs.getString("full_name"),
                UserRole.valueOf(rs.getString("role")),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
