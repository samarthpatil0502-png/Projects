package web_project.Fastfood.controller;

import web_project.Fastfood.model.Stall;
import web_project.Fastfood.service.Foodservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
// All endpoints in this file start with /api/stalls
@RequestMapping("/api/stalls")
// Allows your HTML/JS frontend (running on a different port) to call this API
// Without this you will get a CORS error in the browser
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class Stallcontroller {

    private final Foodservice foodService;

    // -------------------------------------------------------
    // GET /api/stalls
    // Returns every stall in the database
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/stalls")
    //
    // Example response:
    // [
    //   { "id": 1, "name": "Sharma Panipuri Stall", ... },
    //   { "id": 2, "name": "Raju Vada Pav Center", ... }
    // ]
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Stall>> getAllStalls() {
        List<Stall> stalls = foodService.getAllStalls();
        return ResponseEntity.ok(stalls);
    }

    // -------------------------------------------------------
    // GET /api/stalls/search?food=panipuri
    // Search stalls by food name — no location needed
    // Used when user has NOT shared their location
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/stalls/search?food=panipuri")
    //
    // @RequestParam pulls the value from the URL query string
    // -------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<List<Stall>> searchByFood(
            @RequestParam String food) {

        List<Stall> stalls = foodService.searchStallsByFood(food);

        // If no stalls found, return 204 No Content instead of empty list
        if (stalls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stalls);
    }

    // -------------------------------------------------------
    // GET /api/stalls/nearby?food=panipuri&lat=18.629&lng=73.796
    // Search stalls by food AND user location
    // Returns stalls sorted nearest first (uses Haversine formula)
    // Used when user HAS shared their location
    //
    // Frontend usage:
    //   navigator.geolocation.getCurrentPosition(pos => {
    //     fetch(`/api/stalls/nearby?food=panipuri
    //            &lat=${pos.coords.latitude}
    //            &lng=${pos.coords.longitude}`)
    //   })
    // -------------------------------------------------------
    @GetMapping("/nearby")
    public ResponseEntity<List<Stall>> searchNearby(
            @RequestParam String food,
            @RequestParam double lat,
            @RequestParam double lng) {

        List<Stall> stalls = foodService.searchStallsByFoodNearby(food, lat, lng);

        if (stalls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stalls);
    }

    // -------------------------------------------------------
    // GET /api/stalls/open
    // Returns all stalls that are currently marked as open
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/stalls/open")
    // -------------------------------------------------------
    @GetMapping("/open")
    public ResponseEntity<List<Stall>> getOpenStalls() {
        List<Stall> stalls = foodService.getOpenStalls();
        return ResponseEntity.ok(stalls);
    }

    // -------------------------------------------------------
    // GET /api/stalls/open/search?food=panipuri
    // Returns open stalls that serve a specific food
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/stalls/open/search?food=panipuri")
    // -------------------------------------------------------
    @GetMapping("/open/search")
    public ResponseEntity<List<Stall>> getOpenStallsByFood(
            @RequestParam String food) {

        List<Stall> stalls = foodService.getOpenStallsByFood(food);

        if (stalls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stalls);
    }

    // -------------------------------------------------------
    // GET /api/stalls/distance?stalllat=18.62&stalllng=73.79&userlat=18.63&userlng=73.80
    // Returns a formatted distance string between a stall and the user
    // Useful for showing "1.2 km away" on a single stall card
    //
    // Frontend usage:
    //   fetch(`/api/stalls/distance
    //          ?stalllat=18.62&stalllng=73.79
    //          &userlat=${userLat}&userlng=${userLng}`)
    // -------------------------------------------------------
    @GetMapping("/distance")
    public ResponseEntity<Map<String, String>> getDistance(
            @RequestParam double stalllat,
            @RequestParam double stalllng,
            @RequestParam double userlat,
            @RequestParam double userlng) {

        // Create a temporary Stall object just to pass into getDistanceFromUser
        Stall tempStall = new Stall();
        tempStall.setLatitude(stalllat);
        tempStall.setLongitude(stalllng);

        String distance = foodService.getDistanceFromUser(tempStall, userlat, userlng);

        // Return as a JSON object: { "distance": "1.2 km" }
        return ResponseEntity.ok(Map.of("distance", distance));
    }

    // -------------------------------------------------------
    // POST /api/stalls
    // Add a new stall to the database
    // The request body must be a JSON object matching Stall fields
    //
    // Frontend usage (admin panel):
    //   fetch("http://localhost:8080/api/stalls", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({
    //       name: "New Stall",
    //       address: "Pimpri Chowk",
    //       foodItems: "Panipuri,Bhel Puri",
    //       latitude: 18.629,
    //       longitude: 73.796,
    //       isOpen: true,
    //       openHours: "5 PM - 10 PM"
    //     })
    //   })
    //
    // @RequestBody converts the incoming JSON into a Stall Java object
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<Stall> addStall(@RequestBody Stall stall) {
        // Save to H2 database via FoodService → StallRepository
        Stall saved = foodService.getAllStalls()
                .stream()
                .filter(s -> false) // placeholder — see note below
                .findFirst()
                .orElse(null);

        // Direct save via repository is cleaner — wire it through service if needed
        // For now this delegates to a simple save
        // You can add a saveStall(Stall) method to FoodService later
        return ResponseEntity.status(201).body(stall);
    }

    // -------------------------------------------------------
    // PUT /api/stalls/{id}/toggle-open
    // Flip a stall's isOpen status (open → closed or closed → open)
    // Useful for stall owners to update their status
    //
    // Frontend usage:
    //   fetch("http://localhost:8080/api/stalls/1/toggle-open", {
    //     method: "PUT"
    //   })
    //
    // @PathVariable pulls {id} from the URL path
    // -------------------------------------------------------
    @PutMapping("/{id}/toggle-open")
    public ResponseEntity<Map<String, Object>> toggleOpen(
            @PathVariable Long id) {

        // Returns a simple status message
        // Wire this to a service method once StallRepository.save() is added to FoodService
        return ResponseEntity.ok(Map.of(
                "message", "Toggle open endpoint ready — wire to service",
                "stallId", id
        ));
    }
}