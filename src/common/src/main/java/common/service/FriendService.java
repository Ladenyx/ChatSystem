package common.service;

import common.model.FriendRequest;
import common.model.User;
import java.util.List;

/** Service interface for friend and relationship operations. */
public interface FriendService {

    /** Returns the friend list of a user. */
    List<User> getFriends(int userId);

    /** Returns online friends of a user. */
    List<User> getOnlineFriends(int userId);

    /** Returns pending friend requests received by a user. */
    List<FriendRequest> getPendingRequests(int userId);

    /** Sends a friend request from sender to receiver. */
    FriendRequest sendRequest(int senderId, int receiverId);

    /** Accepts a friend request. */
    void acceptRequest(int requestId);

    /** Declines a friend request. */
    void declineRequest(int requestId);

    /** Removes a friend relationship. */
    void unfriend(int userId, int friendId);

    /** Blocks a user (unfriends and prevents new requests). */
    void block(int userId, int targetId);

    /** Returns whether userId is blocked by targetId. */
    boolean isBlocked(int userId, int targetId);

    /** Returns friend counts: [directFriends, friendsOfFriends] for admin analytics. */
    int[] getFriendCounts(int userId);
}
