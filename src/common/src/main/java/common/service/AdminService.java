package common.service;

import common.model.LoginRecord;
import common.model.SpamReport;
import common.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Service interface for admin-only operations. */
public interface AdminService {

    /** Returns the full system login history. */
    List<LoginRecord> getLoginHistory();

    /** Returns login history for a specific user. */
    List<LoginRecord> getUserLoginHistory(int userId);

    /** Returns all spam reports. */
    List<SpamReport> getAllSpamReports();

    /** Returns users active (opened the app) in the given date range.
     *  Each entry: {user, appOpens, peopleChattedWith, groupsChattedIn} */
    List<Map<String, Object>> getActiveUsers(LocalDate from, LocalDate to);

    /** Returns monthly registration counts for the given year (key=month 1-12, value=count). */
    Map<Integer, Long> getMonthlyRegistrations(int year);

    /** Returns monthly active user counts for the given year. */
    Map<Integer, Long> getMonthlyActiveUsers(int year);

    /** Locks the reported user's account from a spam report. */
    void lockFromReport(int reportId);

    /** Marks a spam report as resolved. */
    void resolveReport(int reportId);

    /** Dismisses a spam report. */
    void dismissReport(int reportId);
}
