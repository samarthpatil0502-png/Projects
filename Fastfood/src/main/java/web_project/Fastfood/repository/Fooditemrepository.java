package web_project.Fastfood.repository;

import web_project.Fastfood.model.Fooditem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Fooditemrepository extends JpaRepository<Fooditem, Long> {

    // Find food items by name (case-insensitive)
    // e.g. searching "pani" will match "Panipuri"
    List<Fooditem> findByNameContainingIgnoreCase(String name);

    // Find food items by category
    // e.g. category = "street_food" or "restaurant_food"
    List<Fooditem> findByCategory(String category);

    // Find food items by both name and category
    List<Fooditem> findByNameContainingIgnoreCaseAndCategory(String name, String category);
}