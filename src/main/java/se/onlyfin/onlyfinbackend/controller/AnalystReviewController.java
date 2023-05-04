package se.onlyfin.onlyfinbackend.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.AnalystReviewDTO;
import se.onlyfin.onlyfinbackend.DTO.AnalystReviewPostDTO;
import se.onlyfin.onlyfinbackend.model.AnalystReview;
import se.onlyfin.onlyfinbackend.model.NoSuchUserException;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.repository.AnalystReviewRepository;
import se.onlyfin.onlyfinbackend.repository.UserRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * This class is responsible for handling requests related to analyst reviews.
 */
@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
public class AnalystReviewController {
    private final AnalystReviewRepository analystReviewRepository;
    private final UserRepository userRepository;

    public AnalystReviewController(AnalystReviewRepository analystReviewRepository, UserRepository userRepository) {
        this.analystReviewRepository = analystReviewRepository;
        this.userRepository = userRepository;
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
    public ResponseEntity<String> addReviewForUser(@RequestBody @NotNull AnalystReviewPostDTO analystReviewPostDTO, Principal principal) throws NoSuchUserException {
        if (analystReviewPostDTO.reviewText() == null || analystReviewPostDTO.targetUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        User authorOfReview = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new NoSuchUserException("Could not find any user with that name!"));

        User targetUser = userRepository.findByUsername(analystReviewPostDTO.targetUsername()).orElseThrow(() ->
                new NoSuchUserException("Could not find any user with that name!"));

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
    public ResponseEntity<String> removeReviewForUser(@RequestParam String targetUsername, Principal principal) throws NoSuchUserException {
        User targetUser = userRepository.findByUsername(targetUsername).orElseThrow(() ->
                new NoSuchUserException("No such username found"));

        AnalystReview targetReview = analystReviewRepository
                .findByTargetUserAndAuthorUsername(targetUser, principal.getName()).orElseThrow(() ->
                        new NoSuchElementException("Could not find any review with that ID!"));

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
    public ResponseEntity<AnalystReviewDTO> fetchReviewByAuthorCurrentUserAndTargetUser(@RequestParam String targetUsername, Principal principal) throws NoSuchUserException {
        User targetReviewedUser = userRepository.findByUsername(targetUsername).orElseThrow(() ->
                new NoSuchUserException("No user with that name found!"));

        AnalystReview targetReview = analystReviewRepository
                .findByTargetUserAndAuthorUsername(targetReviewedUser, principal.getName()).orElseThrow(() ->
                        new NoSuchElementException("Could not find any review"));

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
    public ResponseEntity<List<AnalystReviewDTO>> fetchAllReviewsForAnalyst(@RequestParam String targetUsername) throws NoSuchUserException {
        User targetUser = userRepository.findByUsername(targetUsername).orElseThrow(() ->
                new NoSuchUserException("Could not find any user with that name!"));

        List<AnalystReview> reviewList = new ArrayList<>(targetUser.getReviews());
        if (reviewList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<AnalystReviewDTO> reviewDTOList = reviewList.stream()
                .map(currentReview -> new AnalystReviewDTO(
                        currentReview.getAuthorUsername(),
                        currentReview.getReviewText()))
                .toList();

        return ResponseEntity.ok().body(reviewDTOList);
    }

}
