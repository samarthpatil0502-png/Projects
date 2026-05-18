package web_project.Fastfood.repository;

import web_project.Fastfood.model.Stall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Stallrepository extends JpaRepository<Stall, Long> {

    // Search stalls by food item name (case-insensitive)
    // Since foodItems is stored as "Panipuri,Bhel Puri",
    // this checks if the search word appears anywhere in that string
    // e.g. searching "panipuri" will find stalls with "Panipuri" in their foodItems
    List<Stall> findByFoodItemsContainingIgnoreCase(String foodName);

    // Search stalls by stall name
    // e.g. searching "sharma" finds "Sharma Panipuri Stall"
    List<Stall> findByNameContainingIgnoreCase(String name);

    // Find only open stalls
    List<Stall> findByIsOpenTrue();

    // Find open stalls that serve a specific food
    List<Stall> findByFoodItemsContainingIgnoreCaseAndIsOpenTrue(String foodName);

    // Custom query — find stalls within a rough lat/lng bounding box
    // This is a simple distance filter without trigonometry
    // latRange and lngRange are how far (in degrees) to search around the user
    // 0.05 degrees ≈ roughly 5 km
    @Query("SELECT s FROM Stall s WHERE " +
           "s.latitude BETWEEN :lat - :latRange AND :lat + :latRange AND " +
           "s.longitude BETWEEN :lng - :lngRange AND :lng + :lngRange")
    List<Stall> findNearbyStalls(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("latRange") double latRange,
            @Param("lngRange") double lngRange
    );

    // Find nearby stalls that also serve a specific food
    @Query("SELECT s FROM Stall s WHERE " +
           "s.latitude BETWEEN :lat - :latRange AND :lat + :latRange AND " +
           "s.longitude BETWEEN :lng - :lngRange AND :lng + :lngRange AND " +
           "LOWER(s.foodItems) LIKE LOWER(CONCAT('%', :foodName, '%'))")
    List<Stall> findNearbyStallsByFood(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("latRange") double latRange,
            @Param("lngRange") double lngRange,
            @Param("foodName") String foodName
    );
}