package com.chatsystem.common.dto.spam;

import com.chatsystem.common.api.ApiHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Deterministic DEC-10 canonical UTF-8 JSON encoder, 1 MiB size validator, SHA-256 payload fingerprint,
 * and retry-guard tombstone digest calculator shared by User Client and Chat Server.
 */
public final class CanonicalSpamReportEncoder {
    private static final byte[] TOMBSTONE_DOMAIN_PREFIX =
            "ChatSystem:spam-report-request:v1".getBytes(StandardCharsets.UTF_8);
    private static final DateTimeFormatter UTC_MICROS = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 6, 6, true)
            .appendLiteral('Z')
            .toFormatter(Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private CanonicalSpamReportEncoder() {
    }

    /** Encodes a confirmed spam report into exact DEC-10 canonical UTF-8 bytes without BOM or whitespace. */
    public static byte[] encodeCanonicalBytes(UUID reporterUserId, SubmitSpamReportRequest request) {
        Objects.requireNonNull(reporterUserId, "reporterUserId");
        Objects.requireNonNull(request, "request");
        if (reporterUserId.equals(request.reportedUserId())) {
            throw new IllegalArgumentException("reporterUserId and reportedUserId must be distinct");
        }

        List<SpamEvidenceItemDto> orderedEvidence = request.evidence().stream()
                .sorted(Comparator.comparingInt(SpamEvidenceItemDto::displayOrder))
                .toList();

        StringBuilder json = new StringBuilder(512);
        json.append('{');
        appendStringField(json, "id", formatUuid(request.id()), false);
        appendStringField(json, "reporter_user_id", formatUuid(reporterUserId), true);
        appendStringField(json, "reported_user_id", formatUuid(request.reportedUserId()), true);
        appendStringField(json, "conversation_id", formatUuid(request.conversationId()), true);
        appendStringField(json, "reason", request.reason(), true);
        json.append(",\"evidence\":[");
        for (int i = 0; i < orderedEvidence.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            appendEvidenceItem(json, orderedEvidence.get(i));
        }
        json.append("]}");
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Returns {@code true} iff the canonical uncompressed payload fits within DEC-10's 1 MiB (1,048,576 byte) limit. */
    public static boolean fitsSizeLimit(byte[] canonicalBytes) {
        Objects.requireNonNull(canonicalBytes, "canonicalBytes");
        return canonicalBytes.length <= ApiHeaders.MAX_SPAM_REPORT_BYTES;
    }

    /** Computes the 32-byte SHA-256 request fingerprint stored in {@code spam_reports.request_fingerprint}. */
    public static byte[] sha256Fingerprint(byte[] canonicalBytes) {
        Objects.requireNonNull(canonicalBytes, "canonicalBytes");
        return sha256(canonicalBytes);
    }

    /**
     * Computes the 32-byte DEC-10 tombstone digest stored in {@code spam_report_request_tombstones.request_digest}:
     * {@code SHA-256("ChatSystem:spam-report-request:v1" UTF-8 || 0x00 || 16-byte big-endian UUID)}.
     */
    public static byte[] requestTombstoneDigest(UUID reportId) {
        Objects.requireNonNull(reportId, "reportId");
        ByteBuffer buffer = ByteBuffer.allocate(TOMBSTONE_DOMAIN_PREFIX.length + 1 + 16);
        buffer.put(TOMBSTONE_DOMAIN_PREFIX);
        buffer.put((byte) 0x00);
        buffer.putLong(reportId.getMostSignificantBits());
        buffer.putLong(reportId.getLeastSignificantBits());
        return sha256(buffer.array());
    }

    /** Formats raw digest bytes as lowercase hexadecimal for logging and fixture comparisons. */
    public static String toLowerHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        return HexFormat.of().formatHex(bytes);
    }

    private static void appendEvidenceItem(StringBuilder json, SpamEvidenceItemDto item) {
        json.append('{');
        appendStringField(json, "source_message_id", formatUuid(item.sourceMessageId()), false);
        appendStringField(json, "sender_user_id", formatUuid(item.senderUserId()), true);
        appendStringField(json, "message_created_at", formatTimestamp(item.messageCreatedAt()), true);
        appendStringField(json, "content_plain", item.contentPlain(), true);
        json.append(",\"is_selected\":").append(item.selected());
        json.append(",\"is_context\":").append(item.context());
        json.append(",\"display_order\":").append(item.displayOrder());
        json.append('}');
    }

    private static void appendStringField(StringBuilder json, String key, String value, boolean leadingComma) {
        if (leadingComma) {
            json.append(',');
        }
        json.append('"').append(key).append("\":\"");
        appendEscapedJsonString(json, value);
        json.append('"');
    }

    private static void appendEscapedJsonString(StringBuilder json, String value) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (Character.isHighSurrogate(ch)) {
                if (i + 1 >= length || !Character.isLowSurrogate(value.charAt(i + 1))) {
                    throw new IllegalArgumentException("Invalid Unicode string: unpaired high surrogate at index " + i);
                }
                json.append(ch).append(value.charAt(++i));
                continue;
            }
            if (Character.isLowSurrogate(ch)) {
                throw new IllegalArgumentException("Invalid Unicode string: unpaired low surrogate at index " + i);
            }
            appendEscapedBmpChar(json, ch);
        }
    }

    private static void appendEscapedBmpChar(StringBuilder json, char ch) {
        switch (ch) {
            case '"' -> json.append("\\\"");
            case '\\' -> json.append("\\\\");
            case '\b' -> json.append("\\b");
            case '\t' -> json.append("\\t");
            case '\n' -> json.append("\\n");
            case '\f' -> json.append("\\f");
            case '\r' -> json.append("\\r");
            default -> {
                if (ch <= 0x1F) {
                    json.append("\\u00")
                            .append(Character.forDigit((ch >> 4) & 0xF, 16))
                            .append(Character.forDigit(ch & 0xF, 16));
                } else {
                    json.append(ch);
                }
            }
        }
    }

    private static String formatUuid(UUID uuid) {
        return uuid.toString().toLowerCase(Locale.ROOT);
    }

    private static String formatTimestamp(Instant instant) {
        return UTC_MICROS.format(instant);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException unreachable) {
            throw new IllegalStateException("SHA-256 MessageDigest is unavailable", unreachable);
        }
    }
}
