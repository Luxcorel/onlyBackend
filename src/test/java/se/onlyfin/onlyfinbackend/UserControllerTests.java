package se.onlyfin.onlyfinbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.onlyfin.onlyfinbackend.controller.UserController;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.DTO.UserDTO;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class test is responsible for testing the UserController class.
 */
@SpringBootTest
public class UserControllerTests {
    @Mock
    private UserRepository userRepository;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userController = new UserController(userRepository, null);
    }

    /**
     * Test that a new user can be registered if username and email are not already registered in the database.
     * Test that the response body contains the username of the new user
     */
    @Test
    public void testRegisterNewUser() {
        UserDTO userDTO = new UserDTO("testuser", "testpassword", "testemail@test.com");

        Mockito.when(userRepository.existsByEmail(userDTO.email())).thenReturn(false);
        Mockito.when(userRepository.existsByUsername(userDTO.username())).thenReturn(false);

        ResponseEntity<String> response = userController.registerNewUser(userDTO);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDTO.username(), response.getBody());
    }

    /**
     * Test that a new user cannot be registered if email is already registered in the database
     * Test that the response body contains a bad request message saying that the email is already registered
     */
    @Test
    public void testRegisterNewUserWithEmailAlreadyRegistered() {
        UserDTO userDTO = new UserDTO("testuser", "testpassword", "testemail@test.com");

        Mockito.when(userRepository.existsByEmail(userDTO.email())).thenReturn(true);

        ResponseEntity<String> response = userController.registerNewUser(userDTO);

        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any(User.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already registered!", response.getBody());
    }

    /**
     * Test that a new user cannot be registered if username is already taken in the database
     * Test that the response body contains a bad request message saying that the username is already taken
     */
    @Test
    public void testRegisterNewUserWithUsernameAlreadyTaken() {
        UserDTO userDTO = new UserDTO("testuser", "testpassword", "testemail@test.com");

        Mockito.when(userRepository.existsByEmail(userDTO.email())).thenReturn(false);
        Mockito.when(userRepository.existsByUsername(userDTO.username())).thenReturn(true);

        ResponseEntity<String> response = userController.registerNewUser(userDTO);

        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any(User.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
    }

}
