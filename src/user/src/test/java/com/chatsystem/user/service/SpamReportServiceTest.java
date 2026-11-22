package com.chatsystem.user.service;

import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.user.mock.MockConversationCatalog;
import com.chatsystem.user.service.mock.MockSpamReportService;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpamReportServiceTest {
    private final SpamReportService service = new MockSpamReportService();

    @Test
    void testPrepareReportValid() {
        ReportPreparation prep = service.prepareReport(
                "conv-java", "Nhóm Java", "u-nam", "Hoàng Nam",
                "Nội dung không mong muốn, gửi lặp lại.", Set.of("msg-01")
        );

        assertNotNull(prep);
        assertEquals("Hoàng Nam", prep.targetUserName());
        assertEquals("Nội dung không mong muốn, gửi lặp lại.", prep.reason());
        assertEquals(1, prep.selectedCount());
        assertFalse(prep.evidence().isEmpty());
    }

    @Test
    void testValidationFailsOnEmptySelectionOrReason() {
        assertThrows(IllegalArgumentException.class, () ->
                service.prepareReport("conv-java", "Nhóm Java", "u-nam", "Hoàng Nam", "", Set.of("msg-01")));

        assertThrows(IllegalArgumentException.class, () ->
                service.prepareReport("conv-java", "Nhóm Java", "u-nam", "Hoàng Nam", "Lý do", Set.of()));
    }

    @Test
    void testSubmitReport() {
        ReportPreparation prep = service.prepareReport(
                "conv-java", "Nhóm Java", "u-nam", "Hoàng Nam",
                "Lý do", Set.of("msg-01")
        );
        ReportReceipt receipt = service.submitReport(prep);

        assertNotNull(receipt);
        assertEquals("RPT-20261118-001", receipt.reportId());
        assertEquals("PENDING", receipt.status());
    }

    @Test
    void testResolveTargetUserAndChronologicalUnionInGroup() {
        var catalog = new MockConversationCatalog();
        var catalogService = new MockSpamReportService(catalog);
        var options = catalogService.getTargetUserOptions("grp-01");
        String firstOptionId = options.getFirst().id();
        var groupMessages = catalog.messagesFor("grp-01");
        int contextCutoff = Math.max(0, groupMessages.size() - 30);
        MockMessage oldTargetMessage = groupMessages.subList(0, contextCutoff).stream()
                .filter(message -> message.acceptedByServer() && message.state() != MockMessage.State.RECALLED)
                .filter(message -> !catalog.currentUserId().equals(message.senderId())
                        && !firstOptionId.equals(message.senderId()))
                .findFirst().orElseThrow();

        Optional<String> resolved = catalogService.resolveTargetUserId("grp-01", List.of(oldTargetMessage.id()));
        assertTrue(resolved.isPresent());
        assertEquals(oldTargetMessage.senderId(), resolved.get());

        String targetName = catalog.user(oldTargetMessage.senderId()).fullName();
        ReportPreparation prep = catalogService.prepareReport(
                "grp-01", "Nhóm đồ án Java", oldTargetMessage.senderId(), targetName,
                "Gửi tin lặp lại.", Set.of(oldTargetMessage.id()));

        assertEquals(1, prep.selectedCount());
        assertEquals(30, prep.contextCount());
        assertEquals(0, prep.intersectionCount());
        assertEquals(31, prep.totalUniqueCount());
        assertEquals(oldTargetMessage.id(), prep.evidence().getFirst().id());
        assertEquals("Tin được báo cáo", prep.evidence().getFirst().classification());
    }
}
