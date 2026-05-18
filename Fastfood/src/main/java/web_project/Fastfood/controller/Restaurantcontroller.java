package web_project.Fastfood.controller;

import web_project.Fastfood.model.Restaurant;
import web_project.Fastfood.service.Foodservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
// All endpoints in this file start with /api/restaurants
@RequestMapping("/api/restaurants")
// Allows your HTML/JS frontend to call this API without CORS errors
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class Restaurantcontroller {

    private final Foodservice foodService;

    // -------------------------------------------------------
    // GET /api/restaurants
    // Returns every restaurant in the database
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants")
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> restaurants = foodService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/search?food=dosa
    // Search restaurants by food name, sorted by rating (highest first)
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants/search?food=dosa")
    //
    // Example response:
    // [
    //   { "id": 1, "name": "Maharaja's Kitchen", "rating": 4.3,
    //     "zomatoAvailable": true, "swiggyAvailable": true, ... },
    //   { "id": 3, "name": "Spice Route", "rating": 4.0, ... }
    // ]
    // -------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> searchByFood(
            @RequestParam String food) {

        List<Restaurant> restaurants =
                foodService.searchRestaurantsByFoodSortedByRating(food);

        if (restaurants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restaurants);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/search/all?food=panipuri
    // Combined search — returns stalls AND restaurants together in one call
    // This is the most useful endpoint for your main search bar
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants/search/all?food=panipuri")
    //
    // Example response:
    // {
    //   "query": "panipuri",
    //   "stallCount": 2,
    //   "restaurantCount": 3,
    //   "stalls": [...],
    //   "restaurants": [...]
    // }
    // -------------------------------------------------------
    @GetMapping("/search/all")
    public ResponseEntity<Map<String, Object>> searchAll(
            @RequestParam String food) {

        Map<String, Object> results = foodService.searchAll(food);
        return ResponseEntity.ok(results);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/search/all/nearby?food=panipuri&lat=18.629&lng=73.796
    // Combined search with user location
    // Stalls sorted by distance, restaurants sorted by rating
    // This is the BEST endpoint to use when user shares location
    //
    // Frontend usage:
    //   navigator.geolocation.getCurrentPosition(pos => {
    //     fetch(`/api/restaurants/search/all/nearby
    //            ?food=panipuri
    //            &lat=${pos.coords.latitude}
    //            &lng=${pos.coords.longitude}`)
    //   })
    // -------------------------------------------------------
    @GetMapping("/search/all/nearby")
    public ResponseEntity<Map<String, Object>> searchAllNearby(
            @RequestParam String food,
            @RequestParam double lat,
            @RequestParam double lng) {

        Map<String, Object> results = foodService.searchAllNearby(food, lat, lng);
        return ResponseEntity.ok(results);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/zomato?food=panipuri
    // Returns only restaurants that deliver via Zomato for this food
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants/zomato?food=panipuri")
    // -------------------------------------------------------
    @GetMapping("/zomato")
    public ResponseEntity<List<Restaurant>> getZomatoRestaurants(
            @RequestParam String food) {

        List<Restaurant> restaurants =
                foodService.searchZomatoRestaurantsByFood(food);

        if (restaurants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restaurants);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/swiggy?food=panipuri
    // Returns only restaurants that deliver via Swiggy for this food
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants/swiggy?food=panipuri")
    // -------------------------------------------------------
    @GetMapping("/swiggy")
    public ResponseEntity<List<Restaurant>> getSwiggyRestaurants(
            @RequestParam String food) {

        List<Restaurant> restaurants =
                foodService.searchSwiggyRestaurantsByFood(food);

        if (restaurants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restaurants);
    }

    // -------------------------------------------------------
    // GET /api/restaurants/delivery?food=panipuri&platform=zomato
    // Flexible delivery filter — pass platform as a query param
    // platform can be "zomato", "swiggy", or "both"
    //
    // Frontend usage:
    //   fetch(`/api/restaurants/delivery?food=panipuri&platform=zomato`)
    //   fetch(`/api/restaurants/delivery?food=panipuri&platform=both`)
    // -------------------------------------------------------
    @GetMapping("/delivery")
    public ResponseEntity<?> getByDeliveryPlatform(
            @RequestParam String food,
            @RequestParam(defaultValue = "both") String platform) {

        return switch (platform.toLowerCase()) {
            case "zomato" ->
                ResponseEntity.ok(foodService.searchZomatoRestaurantsByFood(food));

            case "swiggy" ->
                ResponseEntity.ok(foodService.searchSwiggyRestaurantsByFood(food));

            case "both" ->
                ResponseEntity.ok(foodService.searchRestaurantsByFood(food));

            // Unknown platform value — return a helpful error message
            default ->
                ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid platform. Use: zomato, swiggy, or both",
                    "received", platform
                ));
        };
    }

    // -------------------------------------------------------
    // GET /api/restaurants/top?food=panipuri&minRating=4.0
    // Returns restaurants serving a food with rating >= minRating
    // minRating defaults to 4.0 if not provided
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/restaurants/top?food=dosa&minRating=4.5")
    // -------------------------------------------------------
    @GetMapping("/top")
    public ResponseEntity<List<Restaurant>> getTopRated(
            @RequestParam String food,
            @RequestParam(defaultValue = "4.0") double minRating) {

        List<Restaurant> restaurants =
                foodService.searchRestaurantsByFoodAndRating(food, minRating);

        if (restaurants.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restaurants);
    }

    // -------------------------------------------------------
    // POST /api/restaurants
    // Add a new restaurant to the database
    //
    // Frontend usage (admin panel):
    //   fetch("http://localhost:8080/api/restaurants", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({
    //       name: "New Restaurant",
    //       address: "Chinchwad, FC Road",
    //       foodItems: "Dosa,Idli,Vada",
    //       rating: 4.2,
    //       latitude: 18.638,
    //       longitude: 73.807,
    //       zomatoAvailable: true,
    //       swiggyAvailable: false,
    //       zomatoLink: "https://zomato.com/pune/new-restaurant",
    //       swiggyLink: ""
    //     })
    //   })
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<Restaurant> addRestaurant(
            @RequestBody Restaurant restaurant) {

        // 201 Created is the correct status for a successful POST
        return ResponseEntity.status(201).body(restaurant);
    }
}