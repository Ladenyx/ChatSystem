package com.chatsystem.user.controller.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Recommend;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Refine;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Scenario;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Tone;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OfflineAiAssistantServiceTest {
    private final OfflineAiAssistantService service = new OfflineAiAssistantService();

    @Test
    void refineProducesThreeDeterministicOptionsFromTheConfirmedCopyAndTone() {
        assertThrows(IllegalArgumentException.class, () -> new Refine("   ", Tone.CLEAR));
        var request = new Refine("mình đã cập nhật tài liệu.", Tone.POLITE);
        var result = service.generate(request, Scenario.SUCCESS);
        assertEquals(3, result.suggestions().size());
        assertEquals(3, result.suggestions().stream().distinct().count());
        assertEquals(result, service.generate(request, Scenario.SUCCESS));
        assertNotEquals(result, service.generate(new Refine(request.draft(), Tone.SHORT), Scenario.SUCCESS));
    }

    @Test
    void recommendKeepsOnlyTheConfirmedCopiesAndRejectsInvalidSelectionSizes() {
        assertThrows(IllegalArgumentException.class, () -> new Recommend(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Recommend(List.of("ok", " ")));
        assertThrows(IllegalArgumentException.class, () -> new Recommend(List.of("1", "2", "3", "4", "5", "6")));
        var input = new ArrayList<>(List.of("Sớm nhất", "Thứ hai", "Thứ ba", "Thứ tư", "Cuối cùng"));
        var request = new Recommend(input);
        input.clear();
        assertEquals(List.of("Sớm nhất", "Thứ hai", "Thứ ba", "Thứ tư", "Cuối cùng"), request.context());
        assertEquals(3, service.generate(request, Scenario.SUCCESS).suggestions().size());
    }

    @Test
    void simulatedErrorsNeverReturnApplicableSuggestions() {
        var request = new Recommend(List.of("Tin đã xác nhận"));
        for (Scenario scenario : List.of(Scenario.ERROR, Scenario.TIMEOUT, Scenario.QUOTA)) {
            var result = service.generate(request, scenario);
            assertEquals(scenario, result.scenario());
            assertTrue(result.suggestions().isEmpty());
        }
    }
}
