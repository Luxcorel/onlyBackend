package se.onlyfin.onlyfinbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.onlyfin.onlyfinbackend.DTO.CategoryDTO;
import se.onlyfin.onlyfinbackend.DTO.FeedCardDTO;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.DTO.StockDTO;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Category;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Dashboard;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.ModuleEntity;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.Stock;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/feed")
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
public class FeedController {
    private final DashboardController dashboardController;
    private final UserRepository userRepository;

    public FeedController(DashboardController dashboardController, UserRepository userRepository) {
        this.dashboardController = dashboardController;
        this.userRepository = userRepository;
    }

    /**
     * This method fetches all the feed cards for the user that is logged in.
     *
     * @param principal the user that is logged in
     * @return a list of feed cards
     */
    @GetMapping("/all-the-things")
    public ResponseEntity<List<FeedCardDTO>> fetchAllTheFeed(Principal principal) {
        //check that logged-in user exists
        User userToFetchFeedFor = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found!"));

        //check that user has subscriptions
        if (userToFetchFeedFor.getSubscriptions().size() < 1) {
            return ResponseEntity.noContent().build();
        }

        //grab their subscriptions
        List<Subscription> subscriptionList = new ArrayList<>(userToFetchFeedFor.getSubscriptions());

        //assemble a map of dashboards for subscribed-to analysts, so each dashboard can be linked to analyst
        HashMap<User, Dashboard> dashboardOwnershipMap = new HashMap<>();
        for (Subscription currentSubscription : subscriptionList) {
            User ownerOfDashboard =
                    userRepository.findById(currentSubscription.getSubscribedTo().getId()).orElseThrow();

            Dashboard ownersDashboard = dashboardController.getDashboard(
                    currentSubscription
                            .getSubscribedTo()
                            .getId()
            ).getBody();

            dashboardOwnershipMap.put(ownerOfDashboard, ownersDashboard);
        }

        List<FeedCardDTO> feedCardDTOS = new ArrayList<>();
        for (User currentAnalystUser : dashboardOwnershipMap.keySet()) {
            //dashboard of currentAnalystUser
            Dashboard currentDashboard = dashboardOwnershipMap.get(currentAnalystUser);

            //check that the current analyst has a dashboard because that's apparently not always the case...
            if (currentDashboard != null) {
                //profile representation of currentAnalystUser
                ProfileDTO currentAnalystProfileDTO = new ProfileDTO(
                        currentAnalystUser.getUsername(),
                        currentAnalystUser.getId());

                //stocks that currentAnalystUser covers
                List<Stock> stocksCoveredByCurrentAnalyst = new ArrayList<>(currentDashboard.getStocks());

                //go through stocks that currentAnalystUser covers
                for (Stock currentStockThatCurrentAnalystCovers : stocksCoveredByCurrentAnalyst) {
                    //categories under current stock that currentAnalystUser covers
                    for (Category categoryUnderCurrentStockThatCurrentAnalystCovers : currentStockThatCurrentAnalystCovers.getCategories()) {
                        //current module under the current stock category
                        for (ModuleEntity moduleEntityUnderCurrentStockThatCurrentAnalystCovers : categoryUnderCurrentStockThatCurrentAnalystCovers.getModuleEntities()) {
                            JsonNode content = moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getContent();
                            //create "feed card" using all available content
                            feedCardDTOS.add(new FeedCardDTO(
                                    currentAnalystProfileDTO,
                                    new StockDTO(
                                            currentStockThatCurrentAnalystCovers.getName(),
                                            currentStockThatCurrentAnalystCovers.getId()),
                                    new CategoryDTO(
                                            categoryUnderCurrentStockThatCurrentAnalystCovers.getName(),
                                            categoryUnderCurrentStockThatCurrentAnalystCovers.getId()),
                                    content,
                                    LocalDateTime.ofInstant(
                                                    moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getPostDate(),
                                                    ZoneId.systemDefault())
                                            .format(DateTimeFormatter
                                                    .ofPattern("dd MMMM HH:mm yyyy", Locale.ENGLISH)),
                                    LocalDateTime.ofInstant(
                                                    moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getUpdatedDate(),
                                                    ZoneId.systemDefault())
                                            .format(DateTimeFormatter
                                                    .ofPattern("dd/MMMM HH:mm yyyy", Locale.ENGLISH))));
                        }
                    }
                }
            }

        }
        //sort all the things so that the newest content is first(that's why reversed is used)
        feedCardDTOS.sort(Comparator.comparing(FeedCardDTO::postDate).reversed());

        return ResponseEntity.ok().body(feedCardDTOS);
    }

    /**
     * This method fetches feed cards from the last 7 days for the user that is logged in.
     *
     * @param principal the user that is logged in
     * @return a list of feed cards from the last 7 days
     */
    @GetMapping("/week")
    public ResponseEntity<List<FeedCardDTO>> fetchWeeklyFeed(Principal principal) {
        //check that logged-in user exists
        User userToFetchFeedFor = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Username not found!"));

        //check that user has subscriptions
        if (userToFetchFeedFor.getSubscriptions().size() < 1) {
            return ResponseEntity.noContent().build();
        }

        //grab their subscriptions
        List<Subscription> subscriptionList = new ArrayList<>(userToFetchFeedFor.getSubscriptions());

        //assemble a map of dashboards for subscribed-to analysts, so each dashboard can be linked to analyst
        HashMap<User, Dashboard> dashboardOwnershipMap = new HashMap<>();
        for (Subscription currentSubscription : subscriptionList) {
            User ownerOfDashboard =
                    userRepository.findById(currentSubscription.getSubscribedTo().getId()).orElseThrow();

            Dashboard ownersDashboard = dashboardController.getDashboard(
                    currentSubscription
                            .getSubscribedTo()
                            .getId()
            ).getBody();

            dashboardOwnershipMap.put(ownerOfDashboard, ownersDashboard);
        }

        List<FeedCardDTO> feedCardDTOS = new ArrayList<>();
        for (User currentAnalystUser : dashboardOwnershipMap.keySet()) {
            //dashboard of currentAnalystUser
            Dashboard currentDashboard = dashboardOwnershipMap.get(currentAnalystUser);

            //check that the current analyst has a dashboard because that's apparently not always the case...
            if (currentDashboard != null) {
                //profile representation of currentAnalystUser
                ProfileDTO currentAnalystProfileDTO = new ProfileDTO(
                        currentAnalystUser.getUsername(),
                        currentAnalystUser.getId());

                //stocks that currentAnalystUser covers
                List<Stock> stocksCoveredByCurrentAnalyst = new ArrayList<>(currentDashboard.getStocks());

                //go through stocks that currentAnalystUser covers
                for (Stock currentStockThatCurrentAnalystCovers : stocksCoveredByCurrentAnalyst) {
                    //categories under current stock that currentAnalystUser covers
                    for (Category categoryUnderCurrentStockThatCurrentAnalystCovers : currentStockThatCurrentAnalystCovers.getCategories()) {
                        //current module under the current stock category
                        for (ModuleEntity moduleEntityUnderCurrentStockThatCurrentAnalystCovers : categoryUnderCurrentStockThatCurrentAnalystCovers.getModuleEntities()) {
                            if (moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getPostDate().isAfter(Instant.now().minus(7, ChronoUnit.DAYS))) {
                                JsonNode content = moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getContent();
                                //create "feed card" using all available content
                                feedCardDTOS.add(new FeedCardDTO(
                                        currentAnalystProfileDTO,
                                        new StockDTO(
                                                currentStockThatCurrentAnalystCovers.getName(),
                                                currentStockThatCurrentAnalystCovers.getId()),
                                        new CategoryDTO(
                                                categoryUnderCurrentStockThatCurrentAnalystCovers.getName(),
                                                categoryUnderCurrentStockThatCurrentAnalystCovers.getId()),
                                        content,
                                        LocalDateTime.ofInstant(
                                                        moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getPostDate(),
                                                        ZoneId.systemDefault())
                                                .format(DateTimeFormatter
                                                        .ofPattern("dd MMMM HH:mm yyyy", Locale.ENGLISH)),
                                        LocalDateTime.ofInstant(
                                                        moduleEntityUnderCurrentStockThatCurrentAnalystCovers.getUpdatedDate(),
                                                        ZoneId.systemDefault())
                                                .format(DateTimeFormatter
                                                        .ofPattern("dd/MMMM HH:mm yyyy", Locale.ENGLISH))));
                            }
                        }
                    }
                }
            }

        }
        //sort all the things so that the newest content is first(that's why reversed is used)
        feedCardDTOS.sort(Comparator.comparing(FeedCardDTO::postDate).reversed());

        return ResponseEntity.ok().body(feedCardDTOS);
    }

}
