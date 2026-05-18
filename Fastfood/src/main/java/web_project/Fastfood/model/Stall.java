package web_project.Fastfood.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stalls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String foodItems;

    private double latitude;
    private double longitude;

    private boolean isOpen;

    private String openHours;

    private String contactNumber;
}

