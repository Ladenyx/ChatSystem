package com.chatsystem.desktop.mock.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/** Synthetic profile for desktop previews, not an account DTO or authentication identity. */
public record MockUser(String id, String username, String fullName, String email, String address,
                       Optional<LocalDate> birthDate, Gender gender, Role role, Status status,
                       Presence presence, Instant createdAt, Optional<Instant> deletedAt) {
    public enum Gender { FEMALE, MALE, UNSPECIFIED }
    public enum Role { USER, ADMIN }
    public enum Status { ACTIVE, LOCKED, DELETED }
    public enum Presence { ONLINE, OFFLINE }
}
