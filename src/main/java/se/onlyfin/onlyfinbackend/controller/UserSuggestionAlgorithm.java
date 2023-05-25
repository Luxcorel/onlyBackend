package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.DTO.UserRecommendationDTO;
import se.onlyfin.onlyfinbackend.DTO.UserRecommendationStringDTO;
import se.onlyfin.onlyfinbackend.model.FeedCard;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;
import se.onlyfin.onlyfinbackend.repository.FeedCardRepository;
import se.onlyfin.onlyfinbackend.service.UserService;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/algo")
@CrossOrigin(origins = {"https://onlyfrontend-production.up.railway.app", "https://onlyfin.se"}, allowCredentials = "true")
public class UserSuggestionAlgorithm {
    private final DashboardController dashboardController;
    private final UserService userService;
    private final FeedCardRepository feedCardRepository;
    private final SubscriptionController subscriptionController;

    public UserSuggestionAlgorithm(DashboardController dashboardController, UserService userService,
                                   FeedCardRepository feedCardRepository, SubscriptionController subscriptionController) {
        this.dashboardController = dashboardController;
        this.userService = userService;
        this.feedCardRepository = feedCardRepository;
        this.subscriptionController = subscriptionController;
    }

    /**
     * This algorithm returns user profiles that the logged-in user could be interested in.
     * Tries to give the user a list that includes the most active non-subscribed analysts for all stocks the user's
     * subscriptions have.
     * If the user isn't subscribed to anyone, a list of the top most subscribed to users will be returned.
     * A limitation with this algorithm is that it can't recommend analysts that don't cover at least one stock
     * that the user's subscriptions cover.
     *
     * @param principal the logged-in user
     * @return No-content if no suggestions can be made or List if suggestions can be made
     */
    @GetMapping("/by-stocks-covered-weighed-by-post-amount")
    public ResponseEntity<Set<UserRecommendationStringDTO>> byStocksCoveredWeighedByPostAmount(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        //subscription objects
        List<Subscription> subscriptions = new ArrayList<>(fetchingUser.getSubscriptions());
        if (subscriptions.isEmpty()) {
            List<String> mostSubscribedUsernames = subscriptionController.getMostSubscribedUsernames();
            mostSubscribedUsernames.remove(fetchingUser.getUsername());

            Set<ProfileDTO> profiles = userService.getProfilesFromUsernames(mostSubscribedUsernames);

            Set<UserRecommendationStringDTO> recommendations = new HashSet<>();
            for (ProfileDTO profile : profiles) {
                recommendations.add(new UserRecommendationStringDTO("Popular user", profile));
            }

            if (recommendations.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok().body(recommendations);
        }

        //all subscriptions
        List<User> subscribedToAnalysts = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            User subscribedTo = subscription.getSubscribedTo();
            subscribedToAnalysts.add(subscribedTo);
        }

        //subscribed-to analysts stock coverage times
        HashMap<String, Integer> commonalityMap = new HashMap<>();
        for (User currentAnalyst : subscribedToAnalysts) {
            List<FeedCard> analystsFeedCards = feedCardRepository.findByAnalystUsername(currentAnalyst.getUsername());
            for (FeedCard currentFeedCard : analystsFeedCards) {
                String stockName = currentFeedCard.getStockName();
                Integer count = commonalityMap.getOrDefault(stockName, 0);
                commonalityMap.put(stockName, count + 1);
            }
        }

        //sort the stocks by occurrences
        List<Map.Entry<String, Integer>> toSort = new ArrayList<>(commonalityMap.entrySet());
        toSort.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        LinkedHashMap<String, Integer> sortedStockOccurrencesForSubscribedAnalysts = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : toSort) {
            sortedStockOccurrencesForSubscribedAnalysts.put(entry.getKey(), entry.getValue());
        }

        //All not-subscribed-to analysts. Will be a bottleneck when scaling
        List<User> notSubscribedToAnalysts = new ArrayList<>(userService.getAllAnalysts());
        notSubscribedToAnalysts.removeIf(subscribedToAnalysts::contains);
        notSubscribedToAnalysts.remove(fetchingUser);
        if (notSubscribedToAnalysts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        //map with available coverage by not-subscribed-to analysts that is also covered by some subscribed-to analyst
        HashMap<String, HashMap<User, Integer>> matches = new HashMap<>();
        for (User currentAnalyst : notSubscribedToAnalysts) {
            for (FeedCard currentFeedCard : feedCardRepository.findByAnalystUsername(currentAnalyst.getUsername())) {
                matches.putIfAbsent(currentFeedCard.getStockName(), new HashMap<>());
                HashMap<User, Integer> mapUnderStockRef = matches.get(currentFeedCard.getStockName());
                Integer currentCountOfCoverage = mapUnderStockRef.getOrDefault(currentAnalyst, 0);
                mapUnderStockRef.put(currentAnalyst, currentCountOfCoverage + 1);
            }
        }

        //suggestion list based on matches between subscribed-to analysts coverage and available coverage
        Set<UserRecommendationStringDTO> suggestions = new HashSet<>();
        for (String currentStockRef : sortedStockOccurrencesForSubscribedAnalysts.keySet()) {
            if (matches.containsKey(currentStockRef)) {
                HashMap<User, Integer> occurrenceMap = matches.get(currentStockRef);

                int highestAmountOfPostsForThisStockRef = -1;
                User winningUser = null;

                for (User currentAnalystThatCoversThisStock : occurrenceMap.keySet()) {
                    if (occurrenceMap.get(currentAnalystThatCoversThisStock) > highestAmountOfPostsForThisStockRef) {
                        highestAmountOfPostsForThisStockRef = occurrenceMap.get(currentAnalystThatCoversThisStock);
                        winningUser = currentAnalystThatCoversThisStock;
                    }
                }

                if (highestAmountOfPostsForThisStockRef != -1) {
                    suggestions.add(new UserRecommendationStringDTO(
                            currentStockRef,
                            new ProfileDTO(winningUser.getUsername(), winningUser.getId())));
                }

            }
        }

        if (suggestions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(suggestions);
    }

    /**
     * Returns the profiles of the 7 most subscribed-to analysts.
     *
     * @param principal the logged-in user
     * @return List with the 7 most subscribed-to users
     */
    @GetMapping("/by-subscription-count-7")
    public ResponseEntity<Set<ProfileDTO>> byTop7SubscriptionCount(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<String> top7MostSubscribedUsernames = subscriptionController.getMostSubscribedUsernames();
        top7MostSubscribedUsernames.remove(fetchingUser.getUsername());

        Set<ProfileDTO> profiles = userService.getProfilesFromUsernames(top7MostSubscribedUsernames);

        if (profiles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(profiles);
    }

    /**
     * This algorithm returns user profiles that the logged-in user could be interested in.
     * Tries to give the user a list that includes the most active non-subscribed analysts for all stocks the user's
     * subscriptions have.
     * A limitation with this algorithm is that it can't recommend anything if the user isn't subscribed to anyone,
     * and it can't recommend analysts that don't cover at least one stock that the user's subscriptions cover.
     *
     * @param principal the logged-in user
     * @return No-content if no suggestions can be made or List if suggestions can be made
     */
    @Deprecated
    public ResponseEntity<List<UserRecommendationDTO>> suggestAnalystsBasedOnCommonStock(Principal principal) {
        //fetch logged-in user
        User fetchingUser = userService.getUserOrException(principal.getName());

        //fetch subscriptions from logged-in user
        List<Subscription> subscriptions = new ArrayList<>(fetchingUser.getSubscriptions());
        if (subscriptions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        //create a User object list of subscriptions
        List<User> subscribedToAnalysts = new ArrayList<>(subscriptions.stream()
                .map(Subscription::getSubscribedTo)
                .toList());

        //create a list of not subscribed-to analysts
        List<User> notSubscribedToAnalysts = new ArrayList<>(userService.getAllAnalysts());
        notSubscribedToAnalysts.removeIf(subscribedToAnalysts::contains);
        notSubscribedToAnalysts.remove(fetchingUser);
        if (notSubscribedToAnalysts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        //Map the strongest commonalities between the subscribed-to analysts.
        //Use the name of stock_ref as a key.
        // Use Integer for how many of the subbed analysts cover that stock
        HashMap<StockRef, Integer> commonalityMap = new HashMap<>();

        //go through all analysts
        for (User currentSubscribedToAnalyst : subscribedToAnalysts) {
            //fetch current analysts dashboard
            Dashboard analystsDashboard = dashboardController.fetchDashboardOrNull(currentSubscribedToAnalyst.getId());
            if (analystsDashboard != null) {
                //fetch stocks under dashboard
                for (Stock currentStock : analystsDashboard.getStocks()) {
                    //grab the current stock reference
                    StockRef currentStockReference = currentStock.getStock_ref_id();
                    //add stock reference to list or increment occurrence count
                    Integer count = commonalityMap.getOrDefault(currentStockReference, 0);
                    commonalityMap.put(currentStockReference, count + 1);
                }
            }
        }

        //occurrence list sorted by the number of occurrences
        LinkedHashMap<StockRef, Integer> sortedStockOccurrencesForSubscribedAnalysts =
                commonalityMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<StockRef, Integer>comparingByValue().reversed())
                        .collect(
                                LinkedHashMap::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                                LinkedHashMap::putAll
                        );

        //map with not subscribed-to analysts and a list of the stocks they cover
        HashMap<StockRef, HashMap<User, Integer>> matches = new HashMap<>();
        //go through all analysts
        for (User currentAnalyst : notSubscribedToAnalysts) {
            //fetch analyst's dashboard
            Dashboard currentDashboard = dashboardController.fetchDashboardOrNull(currentAnalyst.getId());
            if (currentDashboard != null) {
                //fetch all stock objects
                for (Stock currentStock : currentDashboard.getStocks()) {
                    //if the StockRef object isn't already present, insert it along with a new map
                    matches.putIfAbsent(currentStock.getStock_ref_id(), new HashMap<>());
                    //when StockRef object is placed in HashMap,
                    // we want to add the current user along with incrementing the counter
                    //check if user already is present in HashMap, if true increment counter by 1 else gets 0 and +1
                    HashMap<User, Integer> mapUnderStockRef = matches.get(currentStock.getStock_ref_id());
                    Integer currentCountOfCoverage = mapUnderStockRef.getOrDefault(currentAnalyst, 0);
                    mapUnderStockRef.put(currentAnalyst, currentCountOfCoverage + 1);
                }
            }

        }

        //Begin at the top of the occurrence list and check if another analyst covers the most popular stock
        //add the most active poster of that stock then iterate until the end of the list,
        //as it is a set, no analyst will be included twice
        Set<UserRecommendationDTO> suggestions = new HashSet<>();
        for (StockRef currentStockRef : sortedStockOccurrencesForSubscribedAnalysts.keySet()) {
            //at least one analyst covers this stock
            if (matches.containsKey(currentStockRef)) {
                HashMap<User, Integer> occurrenceMap = matches.get(currentStockRef);
                int highestAmountOfPostsForThisStockRef = -1;
                User winningUser = null;
                for (User currentAnalystThatCoversThisStock : occurrenceMap.keySet()) {
                    if (occurrenceMap.get(currentAnalystThatCoversThisStock) > highestAmountOfPostsForThisStockRef) {
                        highestAmountOfPostsForThisStockRef = occurrenceMap.get(currentAnalystThatCoversThisStock);
                        winningUser = currentAnalystThatCoversThisStock;
                    }
                }
                if (highestAmountOfPostsForThisStockRef != -1) {
                    suggestions.add(new UserRecommendationDTO(
                            currentStockRef,
                            new ProfileDTO(winningUser.getUsername(), winningUser.getId())));
                }

            }
        }

        if (suggestions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(suggestions.stream().toList());
    }

}
