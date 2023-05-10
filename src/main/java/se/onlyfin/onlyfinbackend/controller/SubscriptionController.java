package se.onlyfin.onlyfinbackend.controller;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.SubscriptionRepository;
import se.onlyfin.onlyfinbackend.service.UserService;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

/**
 * This class is responsible for handling requests related to subscriptions.
 */
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@RestController
public class SubscriptionController {
    private final SubscriptionRepository subscriptionRepository;
    private final FeedController feedController;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionRepository subscriptionRepository, FeedController feedController,
                                  UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.feedController = feedController;
        this.userService = userService;
    }

    /**
     * Adds a subscription from the logged-in user to the user with the given ID.
     *
     * @param principal the logged-in user
     * @param username  the username of the user to subscribe to
     * @return response entity with the username of the subscribed-to user if successful
     */
    @PostMapping("/subscribe")
    public ResponseEntity<String> addSubscription(@RequestParam("username") String username, Principal principal) {
        User subscribingUser = userService.getUserOrException(principal.getName());

        User userToSubscribeTo = userService.getUserOrNull(username);
        if (userToSubscribeTo == null) {
            return ResponseEntity.notFound().build();
        }
        if (Objects.equals(subscribingUser, userToSubscribeTo)) {
            return ResponseEntity.badRequest().build();
        }

        if (isUserSubscribedToThisUser(subscribingUser, userToSubscribeTo)) {
            return ResponseEntity.badRequest().body("Already subscribed");
        }

        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscribingUser);
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
    public ResponseEntity<String> removeSubscription(@RequestParam("username") String username, Principal principal) {
        User userWantingToUnsubscribe = userService.getUserOrException(principal.getName());

        User userToUnsubscribeFrom = userService.getUserOrNull(username);
        if (userToUnsubscribeFrom == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<Subscription> subscriptionOptional =
                subscriptionRepository.findBySubscriberAndSubscribedTo(userWantingToUnsubscribe, userToUnsubscribeFrom);
        if (subscriptionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Subscription targetSubscription = subscriptionOptional.get();

        subscriptionRepository.delete(targetSubscription);

        return ResponseEntity.ok().body(userToUnsubscribeFrom.getUsername());
    }

    /**
     * Fetches the logged-in user's subscriptions as profiles
     *
     * @param principal the logged-in user
     * @return profile list containing the user's subscriptions
     */
    @GetMapping("/fetch-current-user-subscriptions")
    public ResponseEntity<List<ProfileDTO>> fetchCurrentUserSubscriptions(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<ProfileDTO> profiles = getUserSubscriptionsAsProfiles(fetchingUser);

        return ResponseEntity.ok().body(profiles);
    }

    /**
     * Generates a subscription list sorted by the latest post date of all the analyst posts
     *
     * @param principal the logged-in user
     * @return postdate-sorted analyst profile list
     */
    @GetMapping("/user-subscription-list-sorted-by-postdate")
    public ResponseEntity<List<ProfileDTO>> generateUserSubscriptionListByPostDate(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<Subscription> subscriptions = new ArrayList<>(subscriptionRepository.findBySubscriber(fetchingUser));
        if (subscriptions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<User> subscribedToAnalysts = new ArrayList<>();
        for (Subscription currentSubscription : subscriptions) {
            subscribedToAnalysts.add(currentSubscription.getSubscribedTo());
        }

        //we want the latest poster at the top, therefore, reverse order is used(default is ascending order)
        TreeMap<Instant, User> analystsLastPostTime = new TreeMap<>(Collections.reverseOrder());
        for (User currentAnalyst : subscribedToAnalysts) {
            analystsLastPostTime.put(feedController.fetchAnalystsLastPostTime(currentAnalyst), currentAnalyst);
        }
        List<ProfileDTO> profiles = craftProfileListFromInstantAndUserTreemap(analystsLastPostTime);

        return ResponseEntity.ok().body(profiles);
    }

    /**
     * Generates a subscription list sorted by the latest update date of all the analyst posts
     *
     * @param principal the logged-in user
     * @return updated-date-sorted analyst profile list
     */
    @GetMapping("/user-subscription-list-sorted-by-update-date")
    public ResponseEntity<List<ProfileDTO>> generateUserSubscriptionListByUpdateDate(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<Subscription> subscriptions = new ArrayList<>(subscriptionRepository.findBySubscriber(fetchingUser));
        if (subscriptions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<User> subscribedToAnalysts = new ArrayList<>();
        for (Subscription currentSubscription : subscriptions) {
            subscribedToAnalysts.add(currentSubscription.getSubscribedTo());
        }

        //we want the latest poster at the top, therefore, reverse order is used(default is ascending order)
        TreeMap<Instant, User> analystsLastUpdateTime = new TreeMap<>(Collections.reverseOrder());
        for (User currentAnalyst : subscribedToAnalysts) {
            analystsLastUpdateTime.put(feedController.fetchAnalystsLastUpdateTime(currentAnalyst), currentAnalyst);
        }
        List<ProfileDTO> profiles = craftProfileListFromInstantAndUserTreemap(analystsLastUpdateTime);

        return ResponseEntity.ok().body(profiles);
    }

    /**
     * Fetches the subscription count for the logged-in user
     *
     * @param principal the logged-in user
     * @return the subscription count for the logged-in user
     */
    @GetMapping("/subscriptions/get-my-subscribe-count")
    public ResponseEntity<Integer> fetchSubCountForPrincipal(Principal principal) {
        User targetUser = userService.getUserOrNull(principal.getName());

        Integer subscriptionCount = subscriptionRepository.countAllBySubscribedTo(targetUser);

        return ResponseEntity.ok().body(subscriptionCount);
    }

    /**
     * Fetches the subscription count for the target user
     *
     * @param targetUsername the username of the target user
     * @return the subscription count for the target user
     */
    @GetMapping("/subscriptions/get-subscribe-count")
    public ResponseEntity<Integer> fetchSubCountForTarget(@RequestParam String targetUsername) {
        User targetUser = userService.getUserOrNull(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.badRequest().build();
        }

        Integer subscriptionCount = subscriptionRepository.countAllBySubscribedTo(targetUser);

        return ResponseEntity.ok().body(subscriptionCount);
    }

    /**
     * Checks if the logged-in user is subscribed to the target user
     *
     * @param username  the username of the target user
     * @param principal the logged-in user
     * @return if the logged-in user is subscribed to the target user
     */
    @GetMapping("/subscriptions/is-user-subscribed-to")
    public ResponseEntity<Boolean> isUserSubscribedToTarget(@RequestParam String username, Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        User targetUser = userService.getUserOrNull(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isUserSubscribed = isUserSubscribedToThisUser(fetchingUser, targetUser);

        return ResponseEntity.ok().body(isUserSubscribed);
    }

    /**
     * Helper method to craft a profile list from a treemap of instants and users
     *
     * @param analystsLastUpdateTime the treemap of instants and users
     * @return a profile list in the same order as the treemap
     */
    private List<ProfileDTO> craftProfileListFromInstantAndUserTreemap(TreeMap<Instant, User> analystsLastUpdateTime) {
        List<User> sortedUserList = new ArrayList<>(analystsLastUpdateTime.values());

        List<ProfileDTO> profileDTOList = new ArrayList<>();
        sortedUserList.forEach((currentUser) -> profileDTOList.add(
                new ProfileDTO(currentUser.getUsername(), currentUser.getId())));
        return profileDTOList;
    }

    /**
     * Checks if a user is subscribed to another
     *
     * @param subscriber   the target subscribing user
     * @param subscribedTo the target subscribed-to user
     * @return if the user is subscribed
     */
    public boolean isUserSubscribedToThisUser(User subscriber, User subscribedTo) {
        return subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
    }

    /**
     * @param targetUser the target user
     * @return a list of profiles with the users that the target user is subscribed to
     */
    public List<ProfileDTO> getUserSubscriptionsAsProfiles(@NonNull User targetUser) {
        List<Subscription> subscriptions = new ArrayList<>(subscriptionRepository.findBySubscriber(targetUser));

        List<ProfileDTO> profiles = new ArrayList<>();
        subscriptions.forEach((currentSubscription) -> profiles.add(new ProfileDTO(
                currentSubscription.getSubscribedTo().getUsername(),
                currentSubscription.getSubscribedTo().getId()))
        );

        return profiles;
    }

    /**
     * Removes all subscriptions related to a user
     *
     * @param targetUser the target user
     */
    public void removeAllSubscriptionsRelatedToUser(User targetUser) {
        subscriptionRepository.deleteAllBySubscriber(targetUser);
        subscriptionRepository.deleteAllBySubscribedTo(targetUser);
    }

    public List<String> getTop7MostSubscribedUsernames() {
        return subscriptionRepository.find7MostSubscribedUsernames();
    }

}
