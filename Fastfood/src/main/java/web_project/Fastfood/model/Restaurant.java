package web_project.Fastfood.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String foodItems;

    private double rating;

    private boolean zomatoAvailable;
    private boolean swiggyAvailable;

    private String zomatolink;
    private String swiggylink;

    private String contactNumber;
}
