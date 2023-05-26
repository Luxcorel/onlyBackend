package se.onlyfin.onlyfinbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.onlyfin.onlyfinbackend.DTO.StockRefDTO;
import se.onlyfin.onlyfinbackend.DTO.UserDTO;
import se.onlyfin.onlyfinbackend.model.AnalystReview;
import se.onlyfin.onlyfinbackend.model.Subscription;
import se.onlyfin.onlyfinbackend.model.User;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.*;
import se.onlyfin.onlyfinbackend.repository.*;
import se.onlyfin.onlyfinbackend.service.UserService;

/**
 * This class is responsible for setting up black-box tests of the system.
 */
@RestController
@RequestMapping("/tests")
@CrossOrigin(origins = {"https://onlyfrontend-production.up.railway.app", "https://beta.onlyfin.se"}, allowCredentials = "true")
public class TestSetupController {
    private static final String TEST_PRODUCER_USERNAME = "TEST_PROD";
    private static final String TEST_PRODUCER_PASSWORD = "TEST_PROD";
    private static final String TEST_PRODUCER_EMAIL = "TEST_PROD@example.org";
    private final UserDTO TEST_PRODUCER_DTO = new UserDTO(TEST_PRODUCER_EMAIL, TEST_PRODUCER_USERNAME, TEST_PRODUCER_PASSWORD);

    private static final String TEST_CONSUMER_USERNAME = "TEST_CON";
    private static final String TEST_CONSUMER_PASSWORD = "TEST_CON";
    private static final String TEST_CONSUMER_EMAIL = "TEST_CON@example.org";
    private final UserDTO TEST_CONSUMER_DTO = new UserDTO(TEST_CONSUMER_EMAIL, TEST_CONSUMER_USERNAME, TEST_CONSUMER_PASSWORD);

    private static final String TEST_SUGGESTED_USERNAME = "TEST_SUGGESTED";
    private static final String TEST_SUGGESTED_PASSWORD = "TEST_SUGGESTED";
    private static final String TEST_SUGGESTED_EMAIL = "TEST_SUGGESTED@example.org";
    private final UserDTO TEST_SUGGESTED_DTO = new UserDTO(TEST_SUGGESTED_EMAIL, TEST_SUGGESTED_USERNAME, TEST_SUGGESTED_PASSWORD);

    private final int TEST_STOCK_REF_ID = 0;

    private final UserService userService;
    private final SubscriptionController subscriptionController;
    private final AnalystReviewController analystReviewController;
    private final AnalystReviewRepository analystReviewRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final StockRefRepository stockRefRepository;
    private final ModuleRepository moduleRepository;
    private final DashboardLayoutRepository dashboardLayoutRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public TestSetupController(UserService userService,
                               SubscriptionController subscriptionController,
                               AnalystReviewController analystReviewController,
                               AnalystReviewRepository analystReviewRepository,
                               StockRepository stockRepository,
                               CategoryRepository categoryRepository,
                               StockRefRepository stockRefRepository,
                               ModuleRepository moduleRepository,
                               DashboardLayoutRepository dashboardLayoutRepository,
                               SubscriptionRepository subscriptionRepository) {
        this.userService = userService;
        this.subscriptionController = subscriptionController;
        this.analystReviewController = analystReviewController;
        this.analystReviewRepository = analystReviewRepository;
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.stockRefRepository = stockRefRepository;
        this.moduleRepository = moduleRepository;
        this.dashboardLayoutRepository = dashboardLayoutRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Resets the accounts used for blackbox system testing
     */
    @GetMapping("/reset")
    public void resetAllTheThings() {
        nukeConsumer();
        nukeProducer();
        nukeSuggestedUser();
        setupProducerAndConsumerAccounts();
    }

    /**
     * Sets up the test Registration.1
     */
    @GetMapping("/registration1")
    public void setUpRegistration1() {
        User targetUser = userService.getUserOrNull("TEST_REG");
        if (targetUser != null) {
            userService.deleteUser(targetUser);
        }
    }

    /**
     * Sets up the test Profile.2
     */
    @GetMapping("/profile2")
    public void setUpProfile2() {
        User targetUser = userService.getUserOrException("TEST_PROD");

        AnalystReview analystReview = new AnalystReview();
        analystReview.setAuthorUsername("TEST_CON");
        analystReview.setTargetUser(targetUser);
        analystReview.setReviewText("test review");
        analystReviewRepository.save(analystReview);
    }

    /**
     * Sets up the test Feed.1
     */
    @GetMapping("/feed1")
    @Transactional
    public void setUpFeed1() throws JsonProcessingException {
        User testProducer = userService.getUserOrException(TEST_PRODUCER_USERNAME);
        int dashboardId = testProducer.getId();

        User testConsumer = userService.getUserOrException(TEST_CONSUMER_USERNAME);

        //make consumer subscribe to producer
        Subscription subscription = new Subscription();
        subscription.setSubscriber(testConsumer);
        subscription.setSubscribedTo(testProducer);
        subscriptionRepository.save(subscription);

        postTestChartToDashboard(dashboardId, TEST_STOCK_REF_ID);
    }

    /**
     * Sets up the test Feed.2 (same as Feed.1 at the moment)
     */
    @GetMapping("/feed2")
    public void setUpFeed2() throws JsonProcessingException {
        setUpFeed1();
    }

    /**
     * Sets up the test Feed.3
     */
    @GetMapping("/feed3")
    public void setUpFeed3() throws JsonProcessingException {
        setUpFeed1();
        setupSuggestedAccount();
    }

    /**
     * Sets up the test Dashboard.2
     */
    @GetMapping("/dashboard2")
    public void setUpDashboard2() {
        User testProducer = userService.getUserOrException(TEST_PRODUCER_USERNAME);
        int dashboardId = testProducer.getId();

        createStockAndCategory(dashboardId, TEST_STOCK_REF_ID);
    }

    /**
     * Deletes the consumer account and its related properties
     */
    public void nukeConsumer() {
        User testConsumer = userService.getUserOrNull(TEST_CONSUMER_USERNAME);
        if (testConsumer != null) {
            subscriptionController.removeAllSubscriptionsRelatedToUser(testConsumer);
            analystReviewController.deleteAllReviewsRelatedToUser(testConsumer);
            userService.deleteUser(testConsumer);
        }
    }

    /**
     * Deletes the producer account and its related properties
     */
    public void nukeProducer() {
        User testProducer = userService.getUserOrNull(TEST_PRODUCER_USERNAME);
        if (testProducer != null) {
            subscriptionController.removeAllSubscriptionsRelatedToUser(testProducer);
            analystReviewController.deleteAllReviewsRelatedToUser(testProducer);
            userService.deleteUser(testProducer);
        }
    }

    /**
     * Deletes the suggested user account and its related properties
     */
    public void nukeSuggestedUser() {
        User suggestedUser = userService.getUserOrNull(TEST_SUGGESTED_USERNAME);
        if (suggestedUser != null) {
            subscriptionController.removeAllSubscriptionsRelatedToUser(suggestedUser);
            analystReviewController.deleteAllReviewsRelatedToUser(suggestedUser);
            userService.deleteUser(suggestedUser);
        }
    }

    /**
     * Creates the producer and consumer testing accounts. Won't work if the accounts already exist.
     */
    public void setupProducerAndConsumerAccounts() {
        User testProducer = userService.registerUser(TEST_PRODUCER_DTO);
        User testConsumer = userService.registerUser(TEST_CONSUMER_DTO);
        userService.enableAnalyst(testProducer);
    }

    /**
     * Creates the suggested user testing account. Won't work if the account already exists.
     */
    public void setupSuggestedAccount() throws JsonProcessingException {
        User suggestedUser = userService.registerUser(TEST_SUGGESTED_DTO);
        userService.enableAnalyst(suggestedUser);

        int dashboardId = suggestedUser.getId();

        postTestChartToDashboard(dashboardId, TEST_STOCK_REF_ID);
    }

    /**
     * Posts a chart under a test stock & category for a target dashboard
     *
     * @param dashboardId the target dashboard's id
     * @param testStockRefId the id of the stock reference to use
     * @throws JsonProcessingException if JSON parsing fails
     */
    @Transactional
    public void postTestChartToDashboard(int dashboardId, int testStockRefId) throws JsonProcessingException {
        //create stock
        StockRefDTO stockRefDTO = new StockRefDTO(testStockRefId, dashboardId);
        StockRef stockRef = stockRefRepository.findById(stockRefDTO.stockRefId()).orElseThrow();
        Stock stock = new Stock();
        stock.setStock_ref_id(stockRef);
        stock.setDashboard_id(new Dashboard(stockRefDTO.dashboardId()));
        Stock savedStock = stockRepository.save(stock);

        //create category
        Category category = new Category();
        category.setName("TESTING_CATEGORY");
        category.setStock_id(stock);
        Category savedCategory = categoryRepository.save(category);

        //create module
        String rawJSONChart =
                "{\"chart\": {" +
                        "\"type\": \"column\", " +
                        "\"style\": {" +
                            "\"fontFamily\": \"Tahoma\"}, " +
                            "\"renderTo\": \"container\", " +
                            "\"animation\": {" +
                                "\"easing\": \"linear\", " +
                                "\"duration\": 0}" +
                        "}, " +
                        "\"style\": {" +
                            "\"borderColor\": \"#1A1616\"" +
                        "}, " +
                        "\"title\": {" +
                            "\"text\": \"ONLYTEST\", " +
                            "\"style\": {" +
                                "\"color\": \"#1A1616\", " +
                                "\"fontWeight\": \"lighter\"" +
                            "}" +
                        "}, " +
                        "\"xAxis\": {" +
                            "\"title\": {" +
                                "\"text\": \"\", " +
                                "\"style\": {" +
                                    "\"color\": \"#1A1616\"" +
                                "}" +
                            "}, " +
                            "\"labels\": {" +
                                "\"style\": {" +
                                    "\"color\": \"#1A1616\"" +
                                "}" +
                            "}, " +
                        "\"categories\": [\"Category 1\"], " +
                        "\"gridLineColor\": \"#1A1616\"}, " +
                        "\"yAxis\": {" +
                            "\"title\": {" +
                                "\"text\": \"\", " +
                                "\"style\": {" +
                                    "\"color\": \"#1A1616\"" +
                                "}" +
                            "}, " +
                            "\"labels\": {" +
                                "\"style\": {" +
                                    "\"color\": \"#1A1616\"" +
                                "}" +
                            "}, " +
                        "\"gridLineColor\": \"#1A1616\"" +
                        "}, " +
                        "\"labels\": {" +
                            "\"style\": {" +
                                "\"color\": \"#1A1616\"" +
                            "}" +
                        "}, " +
                        "\"series\": [" +
                            "{\"data\": [100], \"name\": \"name\", \"color\": \"#39a22a\", \"borderWidth\": 0}, " +
                            "{\"data\": [200], \"name\": \"name\", \"color\": \"#da6868\", \"borderWidth\": 0}, " +
                            "{\"data\": [300], \"name\": \"name\", \"color\": \"#a2a852\", \"borderWidth\": 0}" +
                        "], " +
                        "\"exporting\": {" +
                            "\"chartOptions\": {" +
                                "\"subtitle\": {" +
                                    "\"text\": \"Created by TEST_PROD on OnlyFin\", " +
                                    "\"style\": {" +
                                        "\"color\": \"#000\", " +
                                        "\"fontSize\": \"8px\"" +
                                    "}" +
                                "}" +
                            "}" +
                        "}, " +
                        "\"plotOptions\": {" +
                            "\"series\": {" +
                                "\"animation\": {" +
                                    "\"duration\": 0" +
                                "}" +
                            "}" +
                        "}" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode encodedJSONChart = objectMapper.readTree(rawJSONChart);

        ModuleEntity module = new ModuleEntity();
        module.setCategoryId(savedCategory);
        module.setModuleType("column");
        module.setContent(encodedJSONChart);

        ModuleEntity savedModule = moduleRepository.save(module);
        DashboardLayout dashboardLayout = new DashboardLayout(savedModule.getId(), savedModule.getCategory_id());
        dashboardLayoutRepository.save(dashboardLayout);
    }

    /**
     * Creates a Stock and a Category under the specified dashboard ID.
     *
     * @param dashboardId the target dashboard's id
     * @param testStockRefId the id of the stock reference to use
     */
    @Transactional
    public void createStockAndCategory(int dashboardId, int testStockRefId) {
        //create stock
        StockRefDTO stockRefDTO = new StockRefDTO(testStockRefId, dashboardId);
        StockRef stockRef = stockRefRepository.findById(stockRefDTO.stockRefId()).orElseThrow();
        Stock stock = new Stock();
        stock.setStock_ref_id(stockRef);
        stock.setDashboard_id(new Dashboard(stockRefDTO.dashboardId()));
        Stock savedStock = stockRepository.save(stock);

        //create category
        Category category = new Category();
        category.setName("TESTING_CATEGORY");
        category.setStock_id(stock);
        Category savedCategory = categoryRepository.save(category);
    }

}
