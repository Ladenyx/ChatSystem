package com.chatsystem.common.api;

import java.time.ZoneId;

/** Shared protocol constants, headers, limits, and display timezone for REST and WSS contracts. */
public final class ApiHeaders {
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String WS_PATH = "/ws";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CLIENT_DEVICE_ID = "X-Client-Device-Id";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    public static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static final int DEFAULT_PAGE_SIZE = 25;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_MESSAGE_PAGE_SIZE = 50;
    public static final int MAX_BULK_DELETE_SIZE = 50;
    public static final int MAX_SYNC_BATCH_SIZE = 200;

    public static final int MAX_SPAM_SELECTED = 20;
    public static final int MAX_SPAM_CONTEXT = 30;
    public static final int MAX_SPAM_REPORT_BYTES = 1_048_576;

    public static final int AI_MAX_CONTEXT_MESSAGES = 50;
    public static final int AI_SUGGESTION_COUNT = 3;
    public static final int WS_DEFAULT_HEARTBEAT_INTERVAL_SEC = 25;

    private ApiHeaders() {
    }
}
