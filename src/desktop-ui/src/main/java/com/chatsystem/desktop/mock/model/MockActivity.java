package com.chatsystem.desktop.mock.model;

/** Per-user display totals for a mock activity report's selected period. */
public record MockActivity(String userId, int appOpens, int peopleChattedWith, int groupsChattedIn) {
}
