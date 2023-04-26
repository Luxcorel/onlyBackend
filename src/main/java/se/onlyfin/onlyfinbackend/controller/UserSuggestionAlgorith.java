package se.onlyfin.onlyfinbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.DTO.UserRecommendationDTO;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/algo")
@CrossOrigin(origins = "https://onlybackend-production.up.railway.app", allowCredentials = "true")
public class UserSuggestionAlgorith {
    private final UserRepository userRepository;
    private final DashboardController dashboardController;

    public UserSuggestionAlgorith(UserRepository userRepository, DashboardController dashboardController) {
        this.userRepository = userRepository;
        this.dashboardController = dashboardController;
    }

    /**
     * This algorith returns user profiles that the logged-in user could be interested in.
     * Tries to give the user a list that includes the most active non-subscribed analysts for all stocks the user's
     * subscriptions have.
     * A limitation with this algorithm is that it can't recommend anything if the user isn't subscribed to anyone,
     * and it can't recommend analysts that doesn't cover at least one stock that the user's subscriptions cover.
     *
     * @param principal the logged-in user
     * @return No-content if no suggestions can be made or List if suggestions can be made
     */
    @GetMapping("/by-stocks-covered-weighed-by-post-amount")
    public ResponseEntity<List<UserRecommendationDTO>> suggestAnalystsBasedOnCommonStock(Principal principal) {
        //fetch logged-in user
        User userFetchingRecommendedList = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found"));

        //fetch subscriptions from logged-in user
        List<Subscription> subscriptionList = new ArrayList<>(userFetchingRecommendedList.getSubscriptions());
        if (subscriptionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        //create a User object list of subscriptions
        List<User> subscribedToAnalysts = new ArrayList<>(subscriptionList.stream()
                .map(Subscription::getSubscribedTo)
                .toList());

        //create a list of not subscribed-to analysts
        List<User> notSubscribedToAnalystsList = new ArrayList<>(userRepository.findByisAnalystIsTrue());
        notSubscribedToAnalystsList.removeIf(subscribedToAnalysts::contains);
        notSubscribedToAnalystsList.remove(userFetchingRecommendedList);
        if (notSubscribedToAnalystsList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        //Map the strongest commonalities between the subscribed-to analysts.
        //Use name of stock_ref as key. Use Integer for how many of the subbed analysts cover that stock
        HashMap<StockRef, Integer> commonalityMap = new HashMap<>();

        //go through all analysts
        for (User currentSubscribedToAnalyst : subscribedToAnalysts) {
            //fetch current analysts dashboard
            Dashboard analystsDashboard = dashboardController.fetchDashboard(currentSubscribedToAnalyst.getId());
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
        HashMap<StockRef, HashMap<User, Integer>> matchList = new HashMap<>();
        //go through all analysts
        for (User currentAnalyst : notSubscribedToAnalystsList) {
            //fetch analyst's dashboard
            Dashboard currentDashboard = dashboardController.fetchDashboard(currentAnalyst.getId());
            if (currentDashboard != null) {
                //fetch all stock objects
                for (Stock currentStock : currentDashboard.getStocks()) {
                    //if the StockRef object isn't already present, insert it along with a new map
                    matchList.putIfAbsent(currentStock.getStock_ref_id(), new HashMap<>());
                    //when StockRef object is placed in HashMap,
                    // we want to add the current user along with incrementing the counter
                    //check if user already is present in HashMap, if true increment counter by 1 else gets 0 and +1
                    HashMap<User, Integer> mapUnderStockRef = matchList.get(currentStock.getStock_ref_id());
                    Integer currentCountOfCoverage = mapUnderStockRef.getOrDefault(currentAnalyst, 0);
                    mapUnderStockRef.put(currentAnalyst, currentCountOfCoverage + 1);
                }
            }

        }

        //Begin at the top of the occurrence list and check if another analyst covers the most popular stock
        //add the most active poster of that stock then iterate until the end of the list,
        //as it is a set, no analyst will be included twice
        Set<UserRecommendationDTO> suggestionList = new HashSet<>();
        for (StockRef currentStockRef : sortedStockOccurrencesForSubscribedAnalysts.keySet()) {
            //at least one analyst covers this stock
            if (matchList.containsKey(currentStockRef)) {
                HashMap<User, Integer> occurrenceMap = matchList.get(currentStockRef);
                int highestAmountOfPostsForThisStockRef = -1;
                User winningUser = null;
                for (User currentAnalystThatCoversThisStock : occurrenceMap.keySet()) {
                    if (occurrenceMap.get(currentAnalystThatCoversThisStock) > highestAmountOfPostsForThisStockRef) {
                        highestAmountOfPostsForThisStockRef = occurrenceMap.get(currentAnalystThatCoversThisStock);
                        winningUser = currentAnalystThatCoversThisStock;
                    }
                }
                if (highestAmountOfPostsForThisStockRef != -1) {
                    suggestionList.add(new UserRecommendationDTO(
                            currentStockRef,
                            new ProfileDTO(winningUser.getUsername(), winningUser.getId())));
                }

            }
        }

        if (suggestionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(suggestionList.stream().toList());
    }

}
