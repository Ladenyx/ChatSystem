package com.chatsystem.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.AiAssistMode;
import com.chatsystem.common.domain.Gender;
import com.chatsystem.common.domain.MessageDeliveryState;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminFriendNetworkRowDto;
import com.chatsystem.common.dto.admin.AdminOverviewDto;
import com.chatsystem.common.dto.admin.AdminUserDetailDto;
import com.chatsystem.common.dto.admin.MonthlyCountPointDto;
import com.chatsystem.common.dto.ai.AiAssistRequest;
import com.chatsystem.common.dto.ai.AiAssistResponse;
import com.chatsystem.common.dto.auth.AuthNextAction;
import com.chatsystem.common.dto.auth.AuthenticatedUserSummary;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.common.dto.messaging.DeleteMessagesForSelfRequest;
import com.chatsystem.common.dto.messaging.MessageDto;
import com.chatsystem.common.dto.messaging.SendMessageRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class SharedDtosTest {
    private static final Instant NOW = Instant.parse("2026-11-15T03:00:00Z");

    @Test
    void genderMapsCleanlyToAndFromPostgresLabels() {
        assertEquals("Nam", Gender.MALE.databaseValue());
        assertEquals("Nữ", Gender.FEMALE.databaseValue());
        assertEquals("Khác", Gender.OTHER.databaseValue());
        assertEquals(Gender.FEMALE, Gender.fromDatabaseValue("Nữ"));
        assertThrows(IllegalArgumentException.class, () -> Gender.fromDatabaseValue("UNKNOWN"));
    }

    @Test
    void messageDeliveryStateEnforcesMonotonicReceipts() {
        assertTrue(MessageDeliveryState.STORED.canAdvanceReceiptTo(MessageDeliveryState.DELIVERED));
        assertTrue(MessageDeliveryState.DELIVERED.canAdvanceReceiptTo(MessageDeliveryState.READ));
        assertFalse(MessageDeliveryState.READ.canAdvanceReceiptTo(MessageDeliveryState.DELIVERED));
        assertFalse(MessageDeliveryState.PENDING.canAdvanceReceiptTo(MessageDeliveryState.READ));
        assertFalse(MessageDeliveryState.STORED.canAdvanceReceiptTo(MessageDeliveryState.RECALLED));
    }

    @Test
    void loginRequestRequiresLaunchIdOnlyForUserClient() {
        var userLogin = new LoginRequest("minhan", "Secret!2026", SessionScope.USER_CLIENT, UUID.randomUUID());
        assertEquals(SessionScope.USER_CLIENT, userLogin.scope());

        var adminLogin = new LoginRequest("sysadmin", "Secret!2026", SessionScope.ADMIN_APP, null);
        assertEquals(SessionScope.ADMIN_APP, adminLogin.scope());

        assertThrows(IllegalArgumentException.class,
                () -> new LoginRequest("minhan", "Secret!2026", SessionScope.USER_CLIENT, null));
    }

    @Test
    void loginResponseSeparatesSessionFromTransitionTokens() {
        var user = new AuthenticatedUserSummary(
                UUID.randomUUID(), "minhan", "Nguyễn Minh An", "minhan@example.test",
                UserRole.USER, AccountStatus.ACTIVE, true);
        var session = new SessionInfo(UUID.randomUUID(), SessionScope.USER_CLIENT, NOW, user);

        var granted = LoginResponse.sessionGranted("token-123", session);
        assertEquals(AuthNextAction.SESSION_GRANTED, granted.nextAction());

        var forceChange = LoginResponse.forcePasswordChangeRequired("trans-456", NOW.plusSeconds(900));
        assertEquals(AuthNextAction.FORCE_PASSWORD_CHANGE_REQUIRED, forceChange.nextAction());

        assertThrows(IllegalArgumentException.class, () -> new LoginResponse(
                AuthNextAction.FORCE_PASSWORD_CHANGE_REQUIRED, "leak-token", session, "trans", NOW));
    }

    @Test
    void messageDtoMirrorsPostgresPayloadInvariants() {
        UUID msgId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        var direct = new MessageDto(msgId, convId, senderId, NOW, "Xin chào", null, null, null,
                MessageDeliveryState.STORED);
        assertEquals("Xin chào", direct.contentPlain());

        var group = new MessageDto(msgId, convId, senderId, NOW, null, "AAECAw==", Map.of("epochNo", 1), null,
                MessageDeliveryState.DELIVERED);
        assertEquals("AAECAw==", group.contentCipher());

        var recalled = new MessageDto(msgId, convId, senderId, NOW, null, null, null, NOW.plusSeconds(60),
                MessageDeliveryState.RECALLED);
        assertEquals(MessageDeliveryState.RECALLED, recalled.state());

        assertThrows(IllegalArgumentException.class, () -> new MessageDto(
                msgId, convId, senderId, NOW, "both", "AAECAw==", Map.of("epochNo", 1), null,
                MessageDeliveryState.STORED));
        assertThrows(IllegalArgumentException.class, () -> new MessageDto(
                msgId, convId, senderId, NOW, "still-here", null, null, NOW.plusSeconds(10),
                MessageDeliveryState.RECALLED));
    }

    @Test
    void sendAndBulkDeleteValidateBounds() {
        var send = new SendMessageRequest(UUID.randomUUID(), UUID.randomUUID(), "Hello", null, null);
        assertEquals("Hello", send.contentPlain());

        assertThrows(IllegalArgumentException.class,
                () -> new SendMessageRequest(UUID.randomUUID(), null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteMessagesForSelfRequest(List.of()));

        List<UUID> tooMany = IntStream.range(0, 51).mapToObj(i -> UUID.randomUUID()).toList();
        assertThrows(IllegalArgumentException.class, () -> new DeleteMessagesForSelfRequest(tooMany));
    }

    @Test
    void adminDtosEnforceSoftDeleteAndTwelveMonthCharts() {
        UUID id = UUID.randomUUID();
        var active = new AdminUserDetailDto(id, "minhan", "Nguyễn Minh An", "minhan@example.test",
                null, null, Gender.FEMALE, UserRole.USER, AccountStatus.ACTIVE, null, true, NOW, null, null, 3);
        assertEquals(AccountStatus.ACTIVE, active.status());

        assertThrows(IllegalArgumentException.class, () -> new AdminUserDetailDto(
                id, "minhan", "Nguyễn Minh An", "minhan@example.test",
                null, null, null, UserRole.USER, AccountStatus.DELETED, null, true, NOW, null, null, 0));

        List<MonthlyCountPointDto> twelveMonths = IntStream.rangeClosed(1, 12)
                .mapToObj(m -> new MonthlyCountPointDto(2026, m, m))
                .toList();
        var overview = new AdminOverviewDto(20, 5, 4, 2, 2026, twelveMonths, twelveMonths, List.of());
        assertEquals(12, overview.monthlyRegistrations().size());

        assertThrows(IllegalArgumentException.class, () -> new AdminOverviewDto(
                20, 5, 4, 2, 2026, twelveMonths.subList(0, 11), twelveMonths, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new AdminFriendNetworkRowDto(
                id, "minhan", "Nguyễn Minh An", NOW, 5, 4));
    }

    @Test
    void aiAssistEnforcesRefineDraftAndExactlyThreeSuggestions() {
        var recommend = new AiAssistRequest(UUID.randomUUID(), AiAssistMode.RECOMMEND, null, List.of());
        assertEquals(AiAssistMode.RECOMMEND, recommend.mode());

        assertThrows(IllegalArgumentException.class,
                () -> new AiAssistRequest(UUID.randomUUID(), AiAssistMode.REFINE, "   ", List.of()));

        var response = new AiAssistResponse(AiAssistMode.REFINE, List.of("A", "B", "C"));
        assertEquals(3, response.suggestions().size());

        assertThrows(IllegalArgumentException.class,
                () -> new AiAssistResponse(AiAssistMode.RECOMMEND, List.of("A", "B")));
    }
}
