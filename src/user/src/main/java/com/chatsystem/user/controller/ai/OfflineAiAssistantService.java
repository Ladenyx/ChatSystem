package com.chatsystem.user.controller.ai;

import java.util.List;

/** Deterministic responses for the local preview. This service has no network dependency. */
public final class OfflineAiAssistantService {
    public static final int MAX_CONTEXT_MESSAGES = 5;

    public enum Mode { REFINE, RECOMMEND }

    public enum Tone {
        CLEAR("Rõ ràng"), SHORT("Ngắn gọn"), POLITE("Lịch sự");

        private final String label;

        Tone(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum Scenario {
        SUCCESS("Thành công"), ERROR("Lỗi kết nối"), TIMEOUT("Quá thời gian chờ"), QUOTA("Hết hạn mức");

        private final String label;

        Scenario(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public sealed interface Request permits Refine, Recommend {}

    public record Refine(String draft, Tone tone) implements Request {
        public Refine {
            if (draft.isBlank()) {
                throw new IllegalArgumentException("Nhập nội dung muốn chỉnh sửa.");
            }
        }
    }

    public record Recommend(List<String> context) implements Request {
        public Recommend {
            context = List.copyOf(context);
            if (context.isEmpty() || context.size() > MAX_CONTEXT_MESSAGES || context.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Chọn từ 1 đến 5 tin có nội dung hợp lệ.");
            }
        }
    }

    public record Result(Scenario scenario, List<String> suggestions) {}

    public Result generate(Request request, Scenario scenario) {
        if (scenario != Scenario.SUCCESS) {
            return new Result(scenario, List.of());
        }
        var suggestions = switch (request) {
            case Refine refine -> refine(refine);
            case Recommend ignored -> List.of(
                    "Mình đã hiểu rồi, cảm ơn bạn nhé.",
                    "Mình sẽ xem lại và phản hồi bạn sớm.",
                    "Cảm ơn bạn đã cập nhật. Mình sẽ kiểm tra phần này nhé.");
        };
        return new Result(scenario, suggestions);
    }

    private static List<String> refine(Refine request) {
        String input = request.draft().strip().replaceAll("[ \\t]+", " ");
        String sentence = capitalize(input);
        return switch (request.tone()) {
            case CLEAR -> List.of(sentence, "Mình xin cập nhật: " + input, "Thông tin mình muốn chia sẻ: " + input);
            case SHORT -> List.of(sentence, "Tóm tắt: " + input, "Cập nhật nhanh: " + input);
            case POLITE -> List.of("Mình xin chia sẻ: " + input, sentence + " Cảm ơn bạn nhé.",
                    "Bạn xem giúp mình thông tin này nhé: " + input);
        };
    }

    private static String capitalize(String value) {
        int first = value.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(first)))
                + value.substring(Character.charCount(first));
    }
}
