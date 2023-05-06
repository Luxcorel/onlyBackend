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

        List<ProfileDTO> usersToReturnToClient = new ArrayList<>();
        foundUsers.forEach((currentUser ->
                usersToReturnToClient.add(new ProfileDTO(currentUser.getUsername(), currentUser.getId()))));

        return ResponseEntity.ok(usersToReturnToClient);
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
        ProfileDTO profileDTOToSendToClient = new ProfileDTO(fetchedUser.getUsername(), fetchedUser.getId());

        return ResponseEntity.ok().body(profileDTOToSendToClient);
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

        List<User> userList = new ArrayList<>(userService.findAnalystWithUsernameStartingWith(search));
        userList.remove(fetchingUser);
        if (userList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> profileDTOListToSend = new ArrayList<>();
        userList.forEach((currentUser) ->
                profileDTOListToSend.add(new ProfileDTO(currentUser.getUsername(), currentUser.getId())));

        return ResponseEntity.ok().body(profileDTOListToSend);
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

        List<User> userList = new ArrayList<>(userService.findAnalystWithUsernameStartingWith(search));
        userList.remove(fetchingUser);
        if (userList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> searchResults = new ArrayList<>();
        userList.forEach((currentUser) ->
                searchResults.add(new ProfileDTO(currentUser.getUsername(), currentUser.getId())));

        List<ProfileDTO> loggedInUserSubscriptions =
                subscriptionController.fetchCurrentUserSubscriptions(principal).getBody();
        if (loggedInUserSubscriptions == null) {
            List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubscribingFalseInfo = new ArrayList<>();

            searchResults.forEach((currentResult) ->
                    profileListWithSubscribingFalseInfo.add(
                            new ProfileWithSubInfoForLoggedInUserDTO(currentResult, false)));

            return ResponseEntity.ok().body(profileListWithSubscribingFalseInfo);
        }

        List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubInfo = new ArrayList<>();
        searchResults.forEach((currentResult) -> profileListWithSubInfo.add(new ProfileWithSubInfoForLoggedInUserDTO(
                currentResult, loggedInUserSubscriptions.contains(currentResult))));

        return ResponseEntity.ok().body(profileListWithSubInfo);
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

        List<User> userList = new ArrayList<>(userService.getAllAnalysts());
        userList.remove(fetchingUser);
        if (userList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfileDTO> searchResults = new ArrayList<>();
        userList.forEach((currentUser) ->
                searchResults.add(new ProfileDTO(currentUser.getUsername(), currentUser.getId())));

        List<ProfileDTO> loggedInUserSubscriptions =
                subscriptionController.fetchCurrentUserSubscriptions(principal).getBody();
        if (loggedInUserSubscriptions == null) {
            List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubscribingFalseInfo = new ArrayList<>();

            searchResults.forEach((currentResult) ->
                    profileListWithSubscribingFalseInfo.add(
                            new ProfileWithSubInfoForLoggedInUserDTO(currentResult, false)));

            return ResponseEntity.ok().body(profileListWithSubscribingFalseInfo);
        }

        List<ProfileWithSubInfoForLoggedInUserDTO> profileListWithSubInfo = new ArrayList<>();
        searchResults.forEach((currentResult) -> profileListWithSubInfo.add(new ProfileWithSubInfoForLoggedInUserDTO(
                currentResult, loggedInUserSubscriptions.contains(currentResult))));

        return ResponseEntity.ok().body(profileListWithSubInfo);
    }

    /**
     * Finds analysts that cover a target stock
     *
     * @param principal the logged-in user
     * @param stockName name of the target stock
     * @return list of analysts covering target stock
     */
    @GetMapping("/find-analysts-that-cover-stock")
    public ResponseEntity<List<ProfileDTO>> findAnalystsThatCoverStock(Principal principal, @RequestParam String stockName) {
        User fetchingUser = userService.getUserOrException(principal.getName());

        StockRef targetStock = stockReferenceController.fetchStockRefByName(stockName).orElseThrow();

        ArrayList<User> analystList = new ArrayList<>(userService.getAllAnalysts());
        analystList.remove(fetchingUser);
        if (analystList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        HashMap<StockRef, ArrayList<User>> coverageMap = dashboardController.fetchCoverageMap(analystList);
        HashSet<ProfileDTO> analystsCoveringTargetStocks = new HashSet<>();
        if (coverageMap.containsKey(targetStock)) {
            coverageMap.get(targetStock).forEach((currentAnalyst) -> analystsCoveringTargetStocks.add(
                    new ProfileDTO(currentAnalyst.getUsername(), currentAnalyst.getId())));
        }

        if (analystsCoveringTargetStocks.isEmpty()) {
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(analystsCoveringTargetStocks.stream().toList());
    }

}
