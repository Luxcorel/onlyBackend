package se.onlyfin.onlyfinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.SubscriptionRepository;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

/**
 * This class is responsible for handling requests related to subscriptions.
 */
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@RestController
public class SubscriptionController {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DashboardController dashboardController;

    @Autowired
    public SubscriptionController(UserRepository userRepository, SubscriptionRepository subscriptionRepository,
                                  DashboardController dashboardController) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.dashboardController = dashboardController;
    }

    /**
     * Adds a subscription from the logged-in user to the user with the given ID.
     *
     * @param principal the logged-in user
     * @param username  the username of the user to subscribe to
     * @return response entity with the username of the subscribed-to user if successful
     */
    @PostMapping("/subscribe")
    public ResponseEntity<String> addSubscription(Principal principal, @RequestParam("username") String username) {
        User userWantingToSubscribe =
                userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                        new UsernameNotFoundException("Logged in user not present in db"));

        //check that authenticated user is not changing other users' subscriptions
        if (!Objects.equals(userWantingToSubscribe.getUsername(), principal.getName())) {
            return ResponseEntity.badRequest().build();
        }

        User userToSubscribeTo = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Subscribe-to user not found with ID " + username));

        if (subscriptionRepository.existsBySubscriberAndSubscribedTo(userWantingToSubscribe, userToSubscribeTo)) {
            return ResponseEntity.badRequest().body("Already subscribed");
        }

        Subscription subscription = new Subscription();
        subscription.setSubscriber(userWantingToSubscribe);
        subscription.setSubscribedTo(userToSubscribeTo);

        subscriptionRepository.save(subscription);

        return ResponseEntity.ok().body(userToSubscribeTo.getUsername());
    }

    /**
     * Removes specified subscription from the logged-in user.
     *
     * @param principal the logged-in user
     * @param username  the username of the user to unsubscribe from
     * @return response entity with the username of the unsubscribed-from user if successful
     */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<String> removeSubscription(Principal principal, @RequestParam("username") String username) {

        User userWantingToUnsubscribe = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Logged in user not present in db"));

        //check that authenticated user is not changing other users' subscriptions
        if (!Objects.equals(userWantingToUnsubscribe.getUsername(), principal.getName())) {
            return ResponseEntity.badRequest().build();
        }

        User userToUnsubscribeFrom = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Subscribed-to user not found with ID " + username));

        Optional<Subscription> subscriptionOptional =
                subscriptionRepository.findBySubscriberAndSubscribedTo(userWantingToUnsubscribe, userToUnsubscribeFrom);

        if (subscriptionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        subscriptionRepository.delete(subscriptionOptional.get());

        return ResponseEntity.ok().body(userToUnsubscribeFrom.getUsername());
    }

    @GetMapping("/fetch-current-user-subscriptions")
    public ResponseEntity<List<ProfileDTO>> fetchCurrentUserSubscriptions(Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User userToFetchSubscriptionsFrom = userOptional.get();

        List<Subscription> subscriptionList = subscriptionRepository.findBySubscriber(userToFetchSubscriptionsFrom);
        List<ProfileDTO> subscriptionsDTOList = new ArrayList<>();
        subscriptionList.forEach((currentSubscription) -> subscriptionsDTOList.add(new ProfileDTO(
                currentSubscription.getSubscribedTo().getUsername(),
                currentSubscription.getSubscribedTo().getId()))
        );

        return ResponseEntity.ok().body(subscriptionsDTOList);
    }

    public boolean isUserSubscribedToThisUser(User subscriber, User subscribedTo) {
        return subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
    }

    /**
     * Generates a subscription list sorted by the latest post date of all the analyst posts
     *
     * @param principal the logged-in user
     * @return postdate-sorted analyst profile list
     */
    @GetMapping("/user-subscription-list-sorted-by-postdate")
    public ResponseEntity<List<ProfileDTO>> generateUserSubscriptionListByPostDate(Principal principal) {
        User userToFetchSubListFor = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found!"));

        List<Subscription> subscriptionList =
                new ArrayList<>(subscriptionRepository.findBySubscriber(userToFetchSubListFor));
        if (subscriptionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<User> subscribedToAnalysts = new ArrayList<>();
        for (Subscription currentSubscription : subscriptionList) {
            subscribedToAnalysts.add(currentSubscription.getSubscribedTo());
        }

        //we want the latest poster at the top, therefore, reverse order is used(default is ascending order)
        TreeMap<Instant, User> analystsLastPostTime = new TreeMap<>(Collections.reverseOrder());
        for (User currentAnalyst : subscribedToAnalysts) {
            analystsLastPostTime.put(dashboardController.fetchAnalystsLastPostTime(currentAnalyst), currentAnalyst);
        }
        List<User> sortedUserList = new ArrayList<>(analystsLastPostTime.values());

        List<ProfileDTO> profileDTOList = new ArrayList<>();
        sortedUserList.forEach((currentUser) -> profileDTOList.add(
                new ProfileDTO(currentUser.getUsername(), currentUser.getId())));

        return ResponseEntity.ok().body(profileDTOList);
    }

    /**
     * Generates a subscription list sorted by the latest update date of all the analyst posts
     *
     * @param principal the logged-in user
     * @return updated-date-sorted analyst profile list
     */
    @GetMapping("/user-subscription-list-sorted-by-update-date")
    public ResponseEntity<List<ProfileDTO>> generateUserSubscriptionListByUpdateDate(Principal principal) {
        User userToFetchSubListFor = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found!"));

        List<Subscription> subscriptionList =
                new ArrayList<>(subscriptionRepository.findBySubscriber(userToFetchSubListFor));
        if (subscriptionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<User> subscribedToAnalysts = new ArrayList<>();
        for (Subscription currentSubscription : subscriptionList) {
            subscribedToAnalysts.add(currentSubscription.getSubscribedTo());
        }

        //we want the latest poster at the top, therefore, reverse order is used(default is ascending order)
        TreeMap<Instant, User> analystsLastPostTime = new TreeMap<>(Collections.reverseOrder());
        for (User currentAnalyst : subscribedToAnalysts) {
            analystsLastPostTime.put(dashboardController.fetchAnalystsLastUpdateTime(currentAnalyst), currentAnalyst);
        }
        List<User> sortedUserList = new ArrayList<>(analystsLastPostTime.values());

        List<ProfileDTO> profileDTOList = new ArrayList<>();
        sortedUserList.forEach((currentUser) -> profileDTOList.add(
                new ProfileDTO(currentUser.getUsername(), currentUser.getId())));

        return ResponseEntity.ok().body(profileDTOList);
    }

}
