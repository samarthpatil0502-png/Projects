package web_project.Fastfood.service;

import web_project.Fastfood.model.Fooditem;
import web_project.Fastfood.model.Restaurant;
import web_project.Fastfood.model.Stall;
import web_project.Fastfood.repository.Fooditemrepository;
import web_project.Fastfood.repository.Restaurantrepository;
import web_project.Fastfood.repository.Stallrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Lombok: generates constructor for all final fields
                          // This replaces needing @Autowired on each field
public class Foodservice {

    // -------------------------------------------------------
    // Dependencies — injected by Spring automatically
    // 'final' + @RequiredArgsConstructor = constructor injection
    // This is the recommended way over @Autowired on fields
    // -------------------------------------------------------
    private final Stallrepository stallRepository;
    private final Restaurantrepository restaurantRepository;
    private final Fooditemrepository foodItemRepository;
    private final Locationservice locationService;

    // Default search radius — 5 km around the user
    private static final double DEFAULT_RADIUS_KM = 5.0;


    // =======================================================
    // FOOD ITEM METHODS
    // =======================================================

    // Get all food items in the database
    public List<Fooditem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    // Search food items by name
    // e.g. query = "pani" returns FoodItem with name "Panipuri"
    public List<Fooditem> searchFoodItems(String query) {
        return foodItemRepository.findByNameContainingIgnoreCase(query);
    }

    // Get food items by category ("street_food" or "restaurant_food")
    public List<Fooditem> getFoodItemsByCategory(String category) {
        return foodItemRepository.findByCategory(category);
    }


    // =======================================================
    // STALL METHODS
    // =======================================================

    // Get all stalls in the database
    public List<Stall> getAllStalls() {
        return stallRepository.findAll();
    }

    // Search stalls by food name only (no location filtering)
    // Used when user hasn't shared their location
    public List<Stall> searchStallsByFood(String foodName) {
        return stallRepository.findByFoodItemsContainingIgnoreCase(foodName);
    }

    // Search stalls by food name AND sort by distance from user
    // Used when user has shared their location
    // Returns stalls within DEFAULT_RADIUS_KM (5 km) sorted nearest first
    public List<Stall> searchStallsByFoodNearby(String foodName,
                                                 double userLat,
                                                 double userLng) {
        // Step 1: Convert km radius to degrees for the bounding box DB query
        double latRange = locationService.kmToLatDegrees(DEFAULT_RADIUS_KM);
        double lngRange = locationService.kmToLngDegrees(DEFAULT_RADIUS_KM, userLat);

        // Step 2: Fetch stalls inside the bounding box that serve this food
        List<Stall> nearbyStalls = stallRepository.findNearbyStallsByFood(
                userLat, userLng, latRange, lngRange, foodName
        );

        // Step 3: Sort the results by actual Haversine distance (nearest first)
        // The bounding box gives us a square area; Haversine gives us a real circle
        return nearbyStalls.stream()
                .sorted(Comparator.comparingDouble(stall ->
                        locationService.calculateDistance(
                                userLat, userLng,
                                stall.getLatitude(), stall.getLongitude()
                        )
                ))
                .collect(Collectors.toList());
    }

    // Get only open stalls
    public List<Stall> getOpenStalls() {
        return stallRepository.findByIsOpenTrue();
    }

    // Get open stalls that serve a specific food
    public List<Stall> getOpenStallsByFood(String foodName) {
        return stallRepository.findByFoodItemsContainingIgnoreCaseAndIsOpenTrue(foodName);
    }

    // Get distance string for a stall from user's location
    // e.g. returns "1.2 km" or "850 m"
    // Called per stall when building the frontend response
    public String getDistanceFromUser(Stall stall, double userLat, double userLng) {
        double distance = locationService.calculateDistance(
                userLat, userLng,
                stall.getLatitude(), stall.getLongitude()
        );
        return locationService.formatDistance(distance);
    }


    // =======================================================
    // RESTAURANT METHODS
    // =======================================================

    // Get all restaurants
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // Search restaurants by food name
    // e.g. query = "dosa" returns all restaurants serving "Dosa"
    public List<Restaurant> searchRestaurantsByFood(String foodName) {
        return restaurantRepository.findByFoodItemsContainingIgnoreCase(foodName);
    }

    // Search restaurants by food AND sort by rating (highest first)
    public List<Restaurant> searchRestaurantsByFoodSortedByRating(String foodName) {
        return restaurantRepository
                .findByFoodItemsContainingIgnoreCase(foodName)
                .stream()
                .sorted(Comparator.comparingDouble(Restaurant::getRating).reversed())
                .collect(Collectors.toList());
    }

    // Get restaurants that deliver on Zomato for a specific food
    public List<Restaurant> searchZomatoRestaurantsByFood(String foodName) {
        return restaurantRepository
                .findByFoodItemsContainingIgnoreCaseAndZomatoAvailableTrue(foodName);
    }

    // Get restaurants that deliver on Swiggy for a specific food
    public List<Restaurant> searchSwiggyRestaurantsByFood(String foodName) {
        return restaurantRepository
                .findByFoodItemsContainingIgnoreCaseAndSwiggyAvailableTrue(foodName);
    }

    // Get restaurants by food AND minimum rating
    // e.g. foodName = "Panipuri", minRating = 4.0
    public List<Restaurant> searchRestaurantsByFoodAndRating(String foodName,
                                                              double minRating) {
        return restaurantRepository
                .findByFoodItemsContainingIgnoreCaseAndRatingGreaterThanEqual(
                        foodName, minRating
                );
    }


    // =======================================================
    // COMBINED SEARCH
    // =======================================================

    // Master search — returns a map with both stalls and restaurants
    // This is what the frontend calls for the main search bar
    // Returns: { "stalls": [...], "restaurants": [...] }
    public Map<String, Object> searchAll(String query) {
        Map<String, Object> results = new LinkedHashMap<>();

        List<Stall> stalls = stallRepository
                .findByFoodItemsContainingIgnoreCase(query);

        List<Restaurant> restaurants = restaurantRepository
                .findByFoodItemsContainingIgnoreCase(query);

        results.put("query", query);
        results.put("stallCount", stalls.size());
        results.put("restaurantCount", restaurants.size());
        results.put("stalls", stalls);
        results.put("restaurants", restaurants);

        return results;
    }

    // Combined search with user location — stalls sorted by distance
    public Map<String, Object> searchAllNearby(String query,
                                                double userLat,
                                                double userLng) {
        Map<String, Object> results = new LinkedHashMap<>();

        List<Stall> stalls = searchStallsByFoodNearby(query, userLat, userLng);
        List<Restaurant> restaurants = searchRestaurantsByFoodSortedByRating(query);

        results.put("query", query);
        results.put("userLat", userLat);
        results.put("userLng", userLng);
        results.put("stallCount", stalls.size());
        results.put("restaurantCount", restaurants.size());
        results.put("stalls", stalls);
        results.put("restaurants", restaurants);

        return results;
    }
}