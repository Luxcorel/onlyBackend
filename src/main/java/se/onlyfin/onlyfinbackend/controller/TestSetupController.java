package se.onlyfin.onlyfinbackend.controller;

import org.springframework.web.bind.annotation.*;
import se.onlyfin.onlyfinbackend.DTO.UserDTO;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.service.UserService;

/**
 * This class is responsible for setting up user accounts that are meant to be used in black-box tests of the system.
 */
@RestController
@RequestMapping("/tests")
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
public class TestSetupController {
    private static final String TEST_PRODUCER_USERNAME = "TEST_PRODUCER_DONT_USE";
    private static final String TEST_PRODUCER_PASSWORD = "TEST_PRODUCER_DONT_USE";
    private static final String TEST_PRODUCER_EMAIL = "TEST_PRODUCER_DONT_USE@example.org";
    private final UserDTO TEST_PRODUCER_DTO;

    private static final String TEST_CONSUMER_USERNAME = "TEST_CONSUMER_DONT_USE";
    private static final String TEST_CONSUMER_PASSWORD = "TEST_CONSUMER_DONT_USE";
    private static final String TEST_CONSUMER_EMAIL = "TEST_CONSUMER_DONT_USE@example.org";
    private final UserDTO TEST_CONSUMER_DTO;

    private final UserService userService;
    private final SubscriptionController subscriptionController;
    private final AnalystReviewController analystReviewController;

    public TestSetupController(UserService userService, SubscriptionController subscriptionController, AnalystReviewController analystReviewController) {
        this.userService = userService;
        this.subscriptionController = subscriptionController;
        this.analystReviewController = analystReviewController;

        TEST_PRODUCER_DTO = new UserDTO(TEST_PRODUCER_EMAIL, TEST_PRODUCER_USERNAME, TEST_PRODUCER_PASSWORD);
        TEST_CONSUMER_DTO = new UserDTO(TEST_CONSUMER_EMAIL, TEST_CONSUMER_USERNAME, TEST_CONSUMER_PASSWORD);
    }

    @GetMapping("/reset")
    public void reset() {
        resetConsumer();
        resetProducer();
        setup();
    }

    public void resetConsumer() {
        User testConsumer = userService.getUserOrNull(TEST_CONSUMER_USERNAME);
        if (testConsumer != null) {
            subscriptionController.removeAllSubscriptionsRelatedToUser(testConsumer);
            analystReviewController.deleteAllReviewsRelatedToUser(testConsumer);
            userService.deleteUser(testConsumer);
        }
    }

    public void resetProducer() {
        User testProducer = userService.getUserOrNull(TEST_PRODUCER_USERNAME);
        if (testProducer != null) {
            subscriptionController.removeAllSubscriptionsRelatedToUser(testProducer);
            analystReviewController.deleteAllReviewsRelatedToUser(testProducer);
            userService.deleteUser(testProducer);
        }
    }

    public void setup() {
        User testProducer = userService.registerUser(TEST_PRODUCER_DTO);
        User testConsumer = userService.registerUser(TEST_CONSUMER_DTO);
        userService.enableAnalyst(testProducer);
    }

}
