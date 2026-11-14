package com.chatsystem.desktop.mock.model;

/** Per-user display totals for MockData.activityFrom() through its fixed snapshot date. */
public record MockActivity(String userId, int appOpens, int peopleChattedWith, int groupsChattedIn) {
}
