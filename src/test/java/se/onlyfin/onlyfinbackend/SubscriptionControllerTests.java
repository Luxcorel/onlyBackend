package se.onlyfin.onlyfinbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import se.onlyfin.onlyfinbackend.controller.SubscriptionController;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.SubscriptionRepository;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class test is responsible for testing the SubscriptionController class.
 */
@SpringBootTest
public class SubscriptionControllerTests {
/*
    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private Principal principal;

    private SubscriptionController subscriptionController;

    @BeforeEach
    public void setup() {
        subscriptionController = new SubscriptionController(userRepository, subscriptionRepository);
    }

    @Test
    public void testAddSubscription() {
        //setup user wanting to subscribe
        User userWantingToSubscribe = new User();
        userWantingToSubscribe.setId(1);
        userWantingToSubscribe.setUsername("userWantingToSubscribe");
        //setup principal and user repository return
        when(principal.getName()).thenReturn("userWantingToSubscribe");
        when(userRepository.findByUsername("userWantingToSubscribe")).thenReturn(Optional.of(userWantingToSubscribe));

        //setup user that will be subscribed to
        User userToSubscribeTo = new User();
        userToSubscribeTo.setId(2);
        userToSubscribeTo.setUsername("userToSubscribeTo");
        //setup user repository return
        when(userRepository.findById(2)).thenReturn(Optional.of(userToSubscribeTo));

        //setup subscription repository to return that no subscription exits
        when(subscriptionRepository.existsBySubscriberAndSubscribedTo(userWantingToSubscribe, userToSubscribeTo)).thenReturn(false);

        //execute test by attempting to subscribe
        ResponseEntity<String> response = subscriptionController.addSubscription(principal, userToSubscribeTo.getUsername());

        //check that a response was received
        assertNotNull(response);
        //check that the status code is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        //check that the response body is the username of the subscribed to user
        assertEquals(userToSubscribeTo.getUsername(), response.getBody());
    }

    @Test
    public void testAddSubscriptionWhereSubscribingUserNotFound() {
        //setup principal
        when(principal.getName()).thenReturn("JohnDoe");

        //setup repository to not contain any user with selected username
        when(userRepository.findByUsername("JohnDoe")).thenReturn(Optional.empty());

        //execute test by trying to subscribe using a nonexistent user
        try {
            //should get UsernameNotFoundException else test has failed
            subscriptionController.addSubscription(principal, "doesNotExist");
            fail("Should have got UsernameNotFoundException");
        } catch (UsernameNotFoundException e) {
            //if test results in UsernameNotFoundException it has passed
            assertNotNull(e);
        }
    }

    @Test
    public void testAddSubscriptionWhereUserToSubscribeToNotFound() {
        //setup principal
        when(principal.getName()).thenReturn("UserWantingToSubscribe");

        //setup user wanting to subscribe
        User userWantingToSubscribe = new User();
        userWantingToSubscribe.setId(1);
        userWantingToSubscribe.setUsername("userWantingToSubscribe");
        //setup principal and user repository return
        when(principal.getName()).thenReturn("userWantingToSubscribe");
        when(userRepository.findByUsername("userWantingToSubscribe")).thenReturn(Optional.of(userWantingToSubscribe));

        //setup repository to not contain any user with subscribeTo ID
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        //execute test by trying to subscribe to a nonexistent user
        try {
            //should get UsernameNotFoundException else test has failed
            subscriptionController.addSubscription(principal, 2);
            fail("Should have got UsernameNotFoundException");
        } catch (UsernameNotFoundException e) {
            //if test results in UsernameNotFoundException it has passed
            assertNotNull(e);
        }
    }

    @Test
    public void testAddSubscriptionWhereAlreadySubscribed() {
        //setup user wanting to subscribe
        User userWantingToSubscribe = new User();
        userWantingToSubscribe.setId(1);
        userWantingToSubscribe.setUsername("userWantingToSubscribe");
        //setup principal and user repository return
        when(principal.getName()).thenReturn("userWantingToSubscribe");
        when(userRepository.findByUsername("userWantingToSubscribe")).thenReturn(Optional.of(userWantingToSubscribe));

        //setup user that will be subscribed to
        User userToSubscribeTo = new User();
        userToSubscribeTo.setId(2);
        userToSubscribeTo.setUsername("userToSubscribeTo");
        //setup user repository return
        when(userRepository.findById(2)).thenReturn(Optional.of(userToSubscribeTo));

        //setup subscription repository to return that a subscription already exits
        when(subscriptionRepository.existsBySubscriberAndSubscribedTo(userWantingToSubscribe, userToSubscribeTo)).thenReturn(true);

        //execute test by attempting to re-subscribe
        ResponseEntity<String> response = subscriptionController.addSubscription(principal, userToSubscribeTo.getId());

        //check that a response was received
        assertNotNull(response);
        //check that a bad request status code was received
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        //check that response body contains "Already subscribed"
        assertEquals("Already subscribed", response.getBody());
    }

    @Test
    public void testRemoveSubscription() {
        //setup user wanting to unsubscribe
        User userWantingToUnsubscribe = new User();
        userWantingToUnsubscribe.setId(1);
        userWantingToUnsubscribe.setUsername("userWantingToUnsubscribe");
        //setup principal and user repository return
        when(principal.getName()).thenReturn("userWantingToUnsubscribe");
        when(userRepository.findByUsername("userWantingToUnsubscribe")).thenReturn(Optional.of(userWantingToUnsubscribe));

        //setup user that will be unsubscribed from
        User userToUnsubscribeFrom = new User();
        userToUnsubscribeFrom.setId(2);
        userToUnsubscribeFrom.setUsername("userToUnsubscribeFrom");
        //setup user repository return
        when(userRepository.findById(2)).thenReturn(Optional.of(userToUnsubscribeFrom));

        //setup subscription repository return
        Subscription subscription = new Subscription();
        subscription.setSubscriber(userWantingToUnsubscribe);
        subscription.setSubscribedTo(userToUnsubscribeFrom);
        when(subscriptionRepository.findBySubscriberAndSubscribedTo(userWantingToUnsubscribe, userToUnsubscribeFrom)).thenReturn(Optional.of(subscription));

        //execute test by attempting to unsubscribe
        ResponseEntity<String> response = subscriptionController.removeSubscription(principal, userToUnsubscribeFrom.getId());

        //check that a response was received
        assertNotNull(response);
        //check that the status code is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        //check that the response body is the username of the unsubscribed from user
        assertEquals(userToUnsubscribeFrom.getUsername(), response.getBody());
        //check that the delete method was called in the subscription database
        verify(subscriptionRepository, times(1)).delete(any(Subscription.class));
    }

    @Test
    public void testRemoveSubscriptionThatDoesNotExist() {
        //setup user wanting to unsubscribe
        User userWantingToUnsubscribe = new User();
        userWantingToUnsubscribe.setId(1);
        userWantingToUnsubscribe.setUsername("userWantingToUnsubscribe");
        //setup principal and user repository return
        when(principal.getName()).thenReturn("userWantingToUnsubscribe");
        when(userRepository.findByUsername("userWantingToUnsubscribe")).thenReturn(Optional.of(userWantingToUnsubscribe));

        //setup user that will be unsubscribed from
        User userToUnsubscribeFrom = new User();
        userToUnsubscribeFrom.setId(2);
        userToUnsubscribeFrom.setUsername("userToUnsubscribeFrom");
        //setup user repository return
        when(userRepository.findById(2)).thenReturn(Optional.of(userToUnsubscribeFrom));

        //setup subscription repository return
        when(subscriptionRepository.findBySubscriberAndSubscribedTo(userWantingToUnsubscribe, userToUnsubscribeFrom)).thenReturn(Optional.empty());

        //execute test by attempting to unsubscribe
        ResponseEntity<String> response = subscriptionController.removeSubscription(principal, userToUnsubscribeFrom.getId());

        //check that a response was received
        assertNotNull(response);
        //check that the status code is NOT FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRemoveSubscriptionWhereSubscribingUserNotFound() {
        //setup principal
        when(principal.getName()).thenReturn("JohnDoe");
        //setup repository return for
        when(userRepository.findByUsername("JohnDoe")).thenReturn(Optional.empty());
        //setup user that will be unsubscribed from
        User userToUnsubscribeFrom = new User();
        userToUnsubscribeFrom.setId(2);
        userToUnsubscribeFrom.setUsername("userToUnsubscribeFrom");
        //setup user repository return
        when(userRepository.findById(2)).thenReturn(Optional.of(userToUnsubscribeFrom));

        //execute test by attempting to unsubscribe
        try {
            subscriptionController.removeSubscription(principal, userToUnsubscribeFrom.getId());
            fail("Should have gotten username not found exception");
        } catch (UsernameNotFoundException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testRemoveSubscriptionWhereUserToSubscribeToNotFound() {
        //setup user wanting to unsubscribe
        User userWantingToUnsubscribe = new User();
        userWantingToUnsubscribe.setId(1);
        userWantingToUnsubscribe.setUsername("userWantingToUnsubscribe");

        //setup principal
        when(principal.getName()).thenReturn(userWantingToUnsubscribe.getUsername());

        //setup repository return
        when(userRepository.findByUsername(userWantingToUnsubscribe.getUsername())).thenReturn(Optional.of(userWantingToUnsubscribe));

        //setup user repository return for not existing user to try to unsubscribe from
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        //execute test by attempting to unsubscribe
        try {
            subscriptionController.removeSubscription(principal, 2);
            fail("Should have gotten username not found exception");
        } catch (UsernameNotFoundException e) {
            assertNotNull(e);
        }
    }
*/
}
