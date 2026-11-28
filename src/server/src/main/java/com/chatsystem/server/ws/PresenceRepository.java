package com.chatsystem.server.ws;

import java.util.List;
import java.util.UUID;

/** Queries eligible non-blocked friends and active 1-1 peers allowed to observe a user's presence. */
public interface PresenceRepository {
    List<UUID> findEligibleContactIds(UUID userId);
}
