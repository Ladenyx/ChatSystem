package com.chatsystem.server.config;

import com.chatsystem.common.api.ApiHeaders;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized ChatSystem server configuration properties under {@code chatsystem.*}. */
@ConfigurationProperties(prefix = "chatsystem")
public record ChatServerProperties(WsProperties ws, SmtpProperties smtp, LlmProperties llm) {
    public ChatServerProperties {
        ws = ws != null ? ws : new WsProperties(25, 60, 10, 500, List.of("*"));
        smtp = smtp != null ? smtp : new SmtpProperties("smtp.gmail.com", 587, "", "");
        llm = llm != null ? llm : new LlmProperties("https://api.meta.ai/v1", "muse-spark-1.3-contributor", "", 15);
    }

    public record WsProperties(
            int heartbeatIntervalSec,
            int heartbeatTimeoutSec,
            int presenceOfflineGraceSec,
            int syncBufferSizePerUser,
            List<String> allowedOrigins
    ) {
        public WsProperties {
            heartbeatIntervalSec = heartbeatIntervalSec > 0
                    ? heartbeatIntervalSec
                    : ApiHeaders.WS_DEFAULT_HEARTBEAT_INTERVAL_SEC;
            heartbeatTimeoutSec = heartbeatTimeoutSec > 0 ? heartbeatTimeoutSec : 60;
            presenceOfflineGraceSec = presenceOfflineGraceSec >= 0 ? presenceOfflineGraceSec : 10;
            syncBufferSizePerUser = syncBufferSizePerUser > 0 ? syncBufferSizePerUser : 500;
            allowedOrigins = (allowedOrigins == null || allowedOrigins.isEmpty())
                    ? List.of("*")
                    : List.copyOf(allowedOrigins);
        }
    }

    public record SmtpProperties(String host, int port, String username, String password) {
        public SmtpProperties {
            host = (host == null || host.isBlank()) ? "smtp.gmail.com" : host;
            port = port > 0 ? port : 587;
            username = username == null ? "" : username;
            password = password == null ? "" : password;
        }
    }

    public record LlmProperties(String baseUrl, String model, String apiKey, int timeoutSec) {
        public LlmProperties {
            baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.meta.ai/v1" : baseUrl;
            model = (model == null || model.isBlank()) ? "muse-spark-1.3-contributor" : model;
            apiKey = apiKey == null ? "" : apiKey;
            timeoutSec = timeoutSec > 0 ? timeoutSec : 15;
        }
    }
}
