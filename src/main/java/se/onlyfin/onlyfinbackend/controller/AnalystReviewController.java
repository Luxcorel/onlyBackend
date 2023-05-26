package se.onlyfin.onlyfinbackend.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.AnalystReviewDTO;
import se.onlyfin.onlyfinbackend.DTO.AnalystReviewPostDTO;
import se.onlyfin.onlyfinbackend.model.AnalystReview;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.AnalystReviewRepository;
import se.onlyfin.onlyfinbackend.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is responsible for handling requests related to analyst reviews.
 */
@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = {"https://onlyfrontend-production.up.railway.app", "https://beta.onlyfin.se"}, allowCredentials = "true")
public class AnalystReviewController {
    private final AnalystReviewRepository analystReviewRepository;
    private final UserService userService;

    public AnalystReviewController(AnalystReviewRepository analystReviewRepository, UserService userService) {
        this.analystReviewRepository = analystReviewRepository;
        this.userService = userService;
    }

    /**
     * This method is responsible for adding or replacing a review for an analyst.
     * Only one review per analyst is allowed per user.
     *
     * @param analystReviewPostDTO the DTO containing the review text and the target analyst's targetUsername.
     * @param principal            the logged-in user.
     * @return A response entity containing the review text.
     */
    @PutMapping("/post")
    @Transactional
    public ResponseEntity<String> addReviewForUser(@RequestBody AnalystReviewPostDTO analystReviewPostDTO, Principal principal) {
        if (analystReviewPostDTO.reviewText() == null || analystReviewPostDTO.targetUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        User authorOfReview = userService.getUserOrException(principal.getName());

        User targetUser = userService.getUserOrNull(analystReviewPostDTO.targetUsername());
        if (targetUser == null) {
            return ResponseEntity.badRequest().body("Could not find any user with that name!");
        }

        //only one review per user is allowed so check that if review already exists and delete if present
        if (analystReviewRepository.findByTargetUserAndAuthorUsername(targetUser, authorOfReview.getUsername()).isPresent()) {
            analystReviewRepository.deleteByTargetUserAndAuthorUsername(targetUser, authorOfReview.getUsername());
        }

        AnalystReview analystReview = new AnalystReview();
        analystReview.setAuthorUsername(authorOfReview.getUsername());
        analystReview.setTargetUser(targetUser);
        analystReview.setReviewText(analystReviewPostDTO.reviewText());
        analystReviewRepository.save(analystReview);

        return ResponseEntity.ok().body(analystReview.getReviewText());
    }

    /**
     * Removes a review using a target username
     * Can only be used to delete the logged-in users reviews
     *
     * @param targetUsername the username of the target reviewed user
     * @param principal      the logged-in user
     * @return Response with "Review deleted"
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> removeReviewForUser(@RequestParam String targetUsername, Principal principal) {
        User targetUser = userService.getUserOrNull(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.badRequest().build();
        }

        AnalystReview targetReview = analystReviewRepository
                .findByTargetUserAndAuthorUsername(targetUser, principal.getName()).orElse(null);
        if (targetReview == null) {
            return ResponseEntity.badRequest().body("Could not find any review with that ID!");
        }

        //user should only be able to delete own review
        if (!Objects.equals(targetReview.getAuthorUsername(), principal.getName())) {
            return ResponseEntity.badRequest().build();
        }

        analystReviewRepository.delete(targetReview);

        return ResponseEntity.ok().body("Review deleted");
    }

    /**
     * Fetches the logged-in user's review of a target user if a review exists
     *
     * @param targetUsername the target username
     * @param principal      the logged-in user
     * @return Response with the review
     */
    @GetMapping("/get-my-review")
    public ResponseEntity<?> fetchReviewByAuthorCurrentUserAndTargetUser(@RequestParam String targetUsername, Principal principal) {
        User targetReviewedUser = userService.getUserOrNull(targetUsername);
        if (targetReviewedUser == null) {
            return ResponseEntity.badRequest().body("No user with that name found!");
        }

        Optional<AnalystReview> optionalTargetReview = analystReviewRepository
                .findByTargetUserAndAuthorUsername(targetReviewedUser, principal.getName());
        if (optionalTargetReview.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        AnalystReview targetReview = optionalTargetReview.get();

        AnalystReviewDTO reviewDTO = new AnalystReviewDTO(
                targetReview.getAuthorUsername(),
                targetReview.getReviewText());

        return ResponseEntity.ok().body(reviewDTO);
    }

    /**
     * Fetches all reviews available for a specified user
     *
     * @param targetUsername username of the target user
     * @return Response with all reviews for the target user
     */
    @GetMapping("/fetch-all")
    public ResponseEntity<List<AnalystReviewDTO>> fetchAllReviewsForAnalyst(@RequestParam String targetUsername) {
        User targetUser = userService.getUserOrNull(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.badRequest().build();
        }

        List<AnalystReview> reviews = new ArrayList<>(targetUser.getReviews());
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<AnalystReviewDTO> reviewDTOs = reviews.stream()
                .map(currentReview -> new AnalystReviewDTO(
                        currentReview.getAuthorUsername(),
                        currentReview.getReviewText()))
                .toList();

        return ResponseEntity.ok().body(reviewDTOs);
    }

    /**
     * Deletes all reviews related to the target user
     *
     * @param targetUser the target user
     */
    @Transactional
    public void deleteAllReviewsRelatedToUser(User targetUser) {
        analystReviewRepository.deleteAllByAuthorUsername(targetUser.getUsername());
        analystReviewRepository.deleteAllByTargetUser(targetUser);
    }

}
