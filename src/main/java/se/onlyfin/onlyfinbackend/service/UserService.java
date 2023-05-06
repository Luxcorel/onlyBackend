package se.onlyfin.onlyfinbackend.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.onlyfin.onlyfinbackend.DTO.UserDTO;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for handling operations regarding user entities.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns a user with the given username if it exists else throws an exception.
     *
     * @param username The username of the user to be returned.
     * @return The user with the given username if it exists.
     */
    public User getUserOrException(@NonNull String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username not found!"));
    }

    /**
     * Returns a user with the given id if it exists else throws an exception.
     *
     * @param id The id of the user to be returned.
     * @return The user with the given id if it exists else throws an exception.
     */
    public User getUserOrException(Integer id) {
        return userRepository.findById(id).orElseThrow();
    }

    /**
     * Returns a user with the given username if it exists else null.
     *
     * @param username The username of the user to be returned.
     * @return The user with the given username if it exists.
     */
    public User getUserOrNull(@NonNull String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * @param id The id of the user to be returned.
     * @return The user with the given id if it exists else null.
     */
    public User getUserOrNull(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * @param user The user details to be checked.
     * @return True if the user is registrable else false.
     */
    public boolean registrable(@Valid UserDTO user) {
        return !userRepository.existsByEmail(user.email()) && !userRepository.existsByUsername(user.username());
    }

    /**
     * Registers a user with the given details if the user is registrable.
     *
     * @param userDTO The details of the user to be registered.
     * @return The registered user if registration was successful else null.
     */
    @Transactional
    public User registerUser(@Valid UserDTO userDTO) {
        if (registrable(userDTO)) {
            User userToRegister = new User();
            userToRegister.setUsername(userDTO.username());
            userToRegister.setPassword(new BCryptPasswordEncoder().encode(userDTO.password()));
            userToRegister.setEmail(userDTO.email());
            userToRegister.setEnabled(true);
            userToRegister.setRoles("ROLE_USER");
            userToRegister.setAnalyst(false);
            return userRepository.save(userToRegister);
        } else {
            return null;
        }
    }

    /**
     * @param oldPasswordConfirmation The old password to be checked.
     * @param currentPassword         The current password to be checked.
     * @return True if the passwords match else false.
     */
    private boolean passwordMatches(String oldPasswordConfirmation, String currentPassword) {
        return passwordEncoder.matches(oldPasswordConfirmation, currentPassword);
    }

    /**
     * @param targetUser              The user to be updated.
     * @param oldPasswordConfirmation The old password to be checked.
     * @param newPassword             The new password to be set.
     * @return True if the password was changed, else false.
     */
    public boolean passwordChange(@NonNull User targetUser, String oldPasswordConfirmation, String newPassword) {
        if (passwordMatches(oldPasswordConfirmation, targetUser.getPassword())) {
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            targetUser.setPassword(encodedNewPassword);
            updateUser(targetUser);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enables analyst status for the given user if the user is not already an analyst.
     *
     * @param targetUser The user to become an analyst.
     * @return True if the user is now an analyst else false.
     */
    public boolean enableAnalyst(@NonNull User targetUser) {
        if (targetUser.isAnalyst()) {
            return false;
        }

        targetUser.setAnalyst(true);
        userRepository.save(targetUser);

        return targetUser.isAnalyst();
    }

    /**
     * Disables analyst status for the given user if the user is an analyst.
     *
     * @param targetUser The user to no longer be an analyst.
     * @return True if the user is no longer an analyst else false.
     */
    public boolean disableAnalyst(@NonNull User targetUser) {
        if (!targetUser.isAnalyst()) {
            return false;
        }

        targetUser.setAnalyst(false);
        userRepository.save(targetUser);

        return !targetUser.isAnalyst();
    }

    /**
     * Saves the given user to the database.
     *
     * @param targetUser The user to be updated.
     * @return The updated user.
     */
    public User updateUser(@NonNull User targetUser) {
        return userRepository.save(targetUser);
    }

    /**
     *
     * @return A list of all analysts.
     */
    public List<User> getAllAnalysts() {
        return userRepository.findByisAnalystIsTrue();
    }

    /**
     * Returns an analyst with the given username if it exists else null.
     *
     * @param username The username of the analyst to be returned.
     * @return The analyst with the given username if it exists else null.
     */
    public Optional<User> getAnalystByName(String username) {
        return userRepository.findByisAnalystIsTrueAndUsernameEquals(username);
    }

    /**
     * Returns a list of analysts with usernames starting with the given search string.
     *
     * @param search The search string to be used.
     * @return A list of analysts with usernames starting with the given search string.
     */
    public List<User> findAnalystWithUsernameStartingWith(String search) {
        return userRepository.findTop7ByisAnalystIsTrueAndUsernameStartsWith(search);
    }
}
