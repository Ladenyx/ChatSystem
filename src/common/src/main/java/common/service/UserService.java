package common.service;

import common.model.User;
import java.util.List;

/**
 * Service interface for user-related operations.
 * Mock implementation used in Version 1; real implementation connects to the server in Version 2.
 */
public interface UserService {

    /** Returns all users (admin use). */
    List<User> getAllUsers();

    /** Searches users by name or username (hides users who blocked the caller). */
    List<User> searchUsers(String query);

    /** Returns a single user by ID. */
    User getUserById(int id);

    /** Returns a user by username. */
    User getUserByUsername(String username);

    /** Creates a new user account. Returns the created user. */
    User createUser(User user, String plainPassword);

    /** Updates user profile. */
    void updateUser(User user);

    /** Deletes a user by ID. */
    void deleteUser(int id);

    /** Locks a user account. */
    void lockUser(int id);

    /** Unlocks a user account. */
    void unlockUser(int id);

    /** Resets a user's password to a randomly generated one and sends it by email. */
    String resetPassword(int id);

    /** Authenticates a user. Returns the user if successful, null otherwise. */
    User authenticate(String username, String plainPassword);

    /** Changes the current user's password. */
    boolean changePassword(int userId, String oldPassword, String newPassword);

    /** Returns users registered in the given date range. */
    List<User> getUsersByRegistrationRange(java.time.LocalDate from, java.time.LocalDate to);
}
