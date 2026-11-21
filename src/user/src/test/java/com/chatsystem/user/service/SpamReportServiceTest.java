package com.chatsystem.user.service;

import com.chatsystem.user.service.mock.MockSpamReportService;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
