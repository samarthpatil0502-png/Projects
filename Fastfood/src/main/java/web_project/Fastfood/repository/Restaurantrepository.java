package web_project.Fastfood.repository;

import web_project.Fastfood.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Restaurantrepository extends JpaRepository<Restaurant, Long> {

    // Search restaurants by food item name (case-insensitive)
    // e.g. searching "dosa" finds restaurants whose foodItems contains "Dosa"
    List<Restaurant> findByFoodItemsContainingIgnoreCase(String foodName);

    // Search restaurants by restaurant name
    // e.g. searching "maharaja" finds "Maharaja's Kitchen"
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // Find restaurants available on Zomato only
    List<Restaurant> findByZomatoAvailableTrue();

    // Find restaurants available on Swiggy only
    List<Restaurant> findBySwiggyAvailableTrue();

    // Find restaurants available on Zomato that serve a specific food
    List<Restaurant> findByFoodItemsContainingIgnoreCaseAndZomatoAvailableTrue(String foodName);

    // Find restaurants available on Swiggy that serve a specific food
    List<Restaurant> findByFoodItemsContainingIgnoreCaseAndSwiggyAvailableTrue(String foodName);

    // Find restaurants available on either Zomato or Swiggy that serve a specific food
    List<Restaurant> findByFoodItemsContainingIgnoreCaseAndZomatoAvailableTrueOrFoodItemsContainingIgnoreCaseAndSwiggyAvailableTrue(
            String foodNameZomato, String foodNameSwiggy
    );

    // Find top rated restaurants (rating >= given value)
    // e.g. findByRatingGreaterThanEqual(4.0) returns 4+ star restaurants
    List<Restaurant> findByRatingGreaterThanEqual(double rating);

    // Find restaurants by food AND minimum rating
    List<Restaurant> findByFoodItemsContainingIgnoreCaseAndRatingGreaterThanEqual(
            String foodName, double rating
    );
}