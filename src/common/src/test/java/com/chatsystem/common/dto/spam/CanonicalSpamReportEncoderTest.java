package com.chatsystem.common.dto.spam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chatsystem.common.api.ApiHeaders;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CanonicalSpamReportEncoderTest {
    private static final UUID REPORT_ID = UUID.fromString("70000000-0000-4000-8000-000000000001");
    private static final UUID REPORTER_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID REPORTED_ID = UUID.fromString("10000000-0000-4000-8000-000000000002");
    private static final UUID CONVERSATION_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final UUID MSG_1_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID MSG_2_ID = UUID.fromString("30000000-0000-4000-8000-000000000002");

    @Test
    void encodesExactDec10KeyOrderMicrosecondUtcTimestampsAndJsonEscapes() {
        // Deliberately pass displayOrder 2 before 1 to verify encoder sorts ascending by display_order.
        var second = new SpamEvidenceItemDto(
                MSG_2_ID,
                REPORTED_ID,
                Instant.parse("2026-11-15T03:00:02.123456Z"),
                "Link: https://spam.example/x \"free\"\n\t\\path\u0007",
                true,
                true,
                2
        );
        var first = new SpamEvidenceItemDto(
                MSG_1_ID,
                REPORTER_ID,
                Instant.parse("2026-11-15T03:00:00Z"),
                "  Xin chào bạn  ",
                false,
                true,
                1
        );
        var request = new SubmitSpamReportRequest(
                REPORT_ID,
                REPORTED_ID,
                CONVERSATION_ID,
                "Gửi link lừa đảo",
                List.of(second, first)
        );

        byte[] canonical = CanonicalSpamReportEncoder.encodeCanonicalBytes(REPORTER_ID, request);
        String json = new String(canonical, StandardCharsets.UTF_8);

        String expected = "{"
                + "\"id\":\"70000000-0000-4000-8000-000000000001\","
                + "\"reporter_user_id\":\"10000000-0000-4000-8000-000000000001\","
                + "\"reported_user_id\":\"10000000-0000-4000-8000-000000000002\","
                + "\"conversation_id\":\"20000000-0000-4000-8000-000000000001\","
                + "\"reason\":\"Gửi link lừa đảo\","
                + "\"evidence\":["
                + "{\"source_message_id\":\"30000000-0000-4000-8000-000000000001\","
                + "\"sender_user_id\":\"10000000-0000-4000-8000-000000000001\","
                + "\"message_created_at\":\"2026-11-15T03:00:00.000000Z\","
                + "\"content_plain\":\"  Xin chào bạn  \","
                + "\"is_selected\":false,\"is_context\":true,\"display_order\":1},"
                + "{\"source_message_id\":\"30000000-0000-4000-8000-000000000002\","
                + "\"sender_user_id\":\"10000000-0000-4000-8000-000000000002\","
                + "\"message_created_at\":\"2026-11-15T03:00:02.123456Z\","
                + "\"content_plain\":\"Link: https://spam.example/x \\\"free\\\"\\n\\t\\\\path\\u0007\","
                + "\"is_selected\":true,\"is_context\":true,\"display_order\":2}"
                + "]}";

        assertEquals(expected, json);
        assertEquals(823, canonical.length);
        assertTrue(CanonicalSpamReportEncoder.fitsSizeLimit(canonical));
        assertEquals(
                "a0595ad9b554aa7148f156cf588b0fc137dc19b523901189d1975104686653e2",
                CanonicalSpamReportEncoder.toLowerHex(CanonicalSpamReportEncoder.sha256Fingerprint(canonical))
        );
        assertEquals(
                "424a190bb9075e03060d8825ca75f7b83f477795cf30c3b155f6c7132b920af3",
                CanonicalSpamReportEncoder.toLowerHex(CanonicalSpamReportEncoder.requestTombstoneDigest(REPORT_ID))
        );
    }

    @Test
    void rejectsInvalidLoneSurrogateUnicodeCharacters() {
        var item = new SpamEvidenceItemDto(
                MSG_1_ID,
                REPORTED_ID,
                Instant.parse("2026-11-15T03:00:00Z"),
                "bad\uD800text",
                true,
                false,
                1
        );
        var request = new SubmitSpamReportRequest(REPORT_ID, REPORTED_ID, CONVERSATION_ID, "Spam", List.of(item));
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalSpamReportEncoder.encodeCanonicalBytes(REPORTER_ID, request));
    }

    @Test
    void enforcesOneMibUncompressedByteLimit() {
        byte[] atLimit = new byte[ApiHeaders.MAX_SPAM_REPORT_BYTES];
        byte[] overLimit = new byte[ApiHeaders.MAX_SPAM_REPORT_BYTES + 1];
        assertTrue(CanonicalSpamReportEncoder.fitsSizeLimit(atLimit));
        assertFalse(CanonicalSpamReportEncoder.fitsSizeLimit(overLimit));
    }

    @Test
    void rejectsSelfReportingOrMissingSelectedEvidence() {
        var contextOnly = new SpamEvidenceItemDto(
                MSG_1_ID, REPORTED_ID, Instant.parse("2026-11-15T03:00:00Z"), "msg", false, true, 1);
        assertThrows(IllegalArgumentException.class, () -> new SubmitSpamReportRequest(
                REPORT_ID, REPORTED_ID, CONVERSATION_ID, "Spam", List.of(contextOnly)));

        var selected = new SpamEvidenceItemDto(
                MSG_1_ID, REPORTED_ID, Instant.parse("2026-11-15T03:00:00Z"), "msg", true, false, 1);
        var validRequest = new SubmitSpamReportRequest(
                REPORT_ID, REPORTED_ID, CONVERSATION_ID, "Spam", List.of(selected));
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalSpamReportEncoder.encodeCanonicalBytes(REPORTED_ID, validRequest));
    }
}
