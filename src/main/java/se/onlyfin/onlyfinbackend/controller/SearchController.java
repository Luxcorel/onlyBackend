package se.onlyfin.onlyfinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.onlyfin.onlyfinbackend.DTO.ProfileDTO;
import se.onlyfin.onlyfinbackend.DTO.ProfileWithSubInfoForLoggedInUserDTO;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.StockRef;
import se.onlyfin.onlyfinbackend.service.UserService;

import java.security.Principal;
import java.util.*;

/**
 * This class is responsible for handling all requests related to searching for analysts.
 */
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@RestController
public class SearchController {
    private final SubscriptionController subscriptionController;
    private final DashboardController dashboardController;
    private final StockReferenceController stockReferenceController;
    private final UserService userService;

    @Autowired
    public SearchController(SubscriptionController subscriptionController,
                            DashboardController dashboardController,
                            StockReferenceController stockReferenceController, UserService userService) {
        this.subscriptionController = subscriptionController;
        this.dashboardController = dashboardController;
        this.stockReferenceController = stockReferenceController;
        this.userService = userService;
    }

    /**
     * This method is responsible for returning a list of all analysts in the database.
     *
     * @return a list of all analysts in the database.
     */
    @GetMapping("/search-all-analysts")
    public ResponseEntity<List<ProfileDTO>> findAllAnalysts(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<User> foundUsers = new ArrayList<>(userService.getAllAnalysts());
        foundUsers.remove(fetchingUser);

        List<ProfileDTO> profiles = createProfileList(foundUsers);

        return ResponseEntity.ok().body(profiles);
    }

    /**
     * This method is responsible for returning a single analyst with the given username.
     *
     * @param username the username of the analyst to be fetched.
     * @return the analyst with the given username.
     */
    @GetMapping("/get-analyst-by-name")
    public ResponseEntity<ProfileDTO> findAnalystByName(@RequestParam String username) {
        Optional<User> userOptional = userService.getAnalystByName(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User fetchedUser = userOptional.get();
        ProfileDTO profile = new ProfileDTO(fetchedUser.getUsername(), fetchedUser.getId());

        return ResponseEntity.ok().body(profile);
    }

    /**
     * This method is responsible for returning a list of analysts that match the given search string.
     *
     * @param search the search string to be used to find analysts.
     * @return a list of analysts that match the search string.
     */
    @GetMapping("/search-analyst")
    public ResponseEntity<List<ProfileDTO>> searchForAnalysts(@RequestParam String search, Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<User> foundUsers = new ArrayList<>(userService.findAnalystWithUsernameStartingWith(search));
        foundUsers.remove(fetchingUser);
        if (foundUsers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> profiles = createProfileList(foundUsers);
        return ResponseEntity.ok().body(profiles);
    }

    /**
     * This method is responsible for returning a list of analysts that match the given search string.
     * This method also includes information about whether the logged-in user is subscribed to a certain analyst.
     *
     * @param search    the search string to be used to find analysts.
     * @param principal the logged-in user.
     * @return a list of analysts that match the search string.
     */
    @GetMapping("/search-analyst-include-sub-info")
    public ResponseEntity<List<ProfileWithSubInfoForLoggedInUserDTO>> searchForAnalystsWithSubsIncluded(@RequestParam String search, Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<User> foundUsers = new ArrayList<>(userService.findAnalystWithUsernameStartingWith(search));
        foundUsers.remove(fetchingUser);
        if (foundUsers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> foundProfiles = createProfileList(foundUsers);

        List<ProfileDTO> subscriptions = subscriptionController.getUserSubscriptionsAsProfiles(fetchingUser);
        if (subscriptions == null) {
            return ResponseEntity.ok().body(getProfilesWithSubscribingFalse(foundProfiles));
        }

        List<ProfileWithSubInfoForLoggedInUserDTO> profilesWithSubInfo = getProfilesWithSubInfo(foundProfiles, subscriptions);
        return ResponseEntity.ok().body(profilesWithSubInfo);
    }

    /**
     * This method is responsible for returning a list of all analysts.
     * This method also includes information about whether the logged-in user is subscribed to a certain analyst.
     *
     * @param principal the logged-in user.
     * @return a list of all analysts.
     */
    @GetMapping("/search-all-analysts-include-sub-info")
    public ResponseEntity<List<ProfileWithSubInfoForLoggedInUserDTO>> fetchAllAnalystsWithSubsIncluded(Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        List<User> analysts = new ArrayList<>(userService.getAllAnalysts());
        analysts.remove(fetchingUser);
        if (analysts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> analystProfiles = createProfileList(analysts);

        List<ProfileDTO> subscriptions = subscriptionController.getUserSubscriptionsAsProfiles(fetchingUser);
        if (subscriptions == null) {
            return ResponseEntity.ok().body(getProfilesWithSubscribingFalse(analystProfiles));
        }

        List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubInfo = getProfilesWithSubInfo(analystProfiles, subscriptions);

        return ResponseEntity.ok().body(profileListWithSubInfo);
    }

    /**
     * Finds analysts that cover an exact target stock name
     *
     * @param principal the logged-in user
     * @param stockName exact name of the target stock
     * @return list of analysts covering target stock
     */
    @GetMapping("/find-analysts-that-cover-stock")
    public ResponseEntity<List<ProfileDTO>> findAnalystsThatCoverStock(@RequestParam String stockName, Principal principal) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        StockRef targetStock = stockReferenceController.fetchStockRefByName(stockName).orElseThrow();

        ArrayList<User> analysts = new ArrayList<>(userService.getAllAnalysts());
        analysts.remove(fetchingUser);
        if (analysts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        HashMap<StockRef, ArrayList<User>> coverageMap = dashboardController.createCoverageMap(analysts);
        HashSet<ProfileDTO> analystsCoveringTargetStock = new HashSet<>();
        if (coverageMap.containsKey(targetStock)) {
            coverageMap.get(targetStock).forEach((currentAnalyst) -> analystsCoveringTargetStock.add(
                    new ProfileDTO(currentAnalyst.getUsername(), currentAnalyst.getId())));
        }

        if (analystsCoveringTargetStock.isEmpty()) {
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(analystsCoveringTargetStock.stream().toList());
    }

    private List<ProfileDTO> createProfileList(List<User> users) {
        List<ProfileDTO> profiles = new ArrayList<>();
        users.forEach((currentUser) ->
                profiles.add(new ProfileDTO(currentUser.getUsername(), currentUser.getId())));
        return profiles;
    }

    private List<ProfileWithSubInfoForLoggedInUserDTO> getProfilesWithSubscribingFalse(List<ProfileDTO> searchResults) {
        List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubscribingFalseInfo = new ArrayList<>();

        searchResults.forEach((currentResult) ->
                profileListWithSubscribingFalseInfo.add(
                        new ProfileWithSubInfoForLoggedInUserDTO(currentResult, false)));

        return profileListWithSubscribingFalseInfo;
    }

    private List<ProfileWithSubInfoForLoggedInUserDTO> getProfilesWithSubInfo(List<ProfileDTO> searchResults, List<ProfileDTO> subscriptions) {
        List<ProfileWithSubInfoForLoggedInUserDTO> profilesWithSubInfo = new ArrayList<>();
        searchResults.forEach((currentResult) -> profilesWithSubInfo.add(new ProfileWithSubInfoForLoggedInUserDTO(
                currentResult, subscriptions.contains(currentResult))));
        return profilesWithSubInfo;
    }

}
