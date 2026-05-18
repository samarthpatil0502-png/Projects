package web_project.Fastfood.service;

import org.springframework.stereotype.Service;

@Service
public class Locationservice {

    // Earth's radius in kilometers — used in Haversine formula
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Default search radius in degrees (0.05 degrees ≈ 5 km)
    public static final double DEFAULT_RANGE = 0.05;

    // -------------------------------------------------------
    // HAVERSINE FORMULA
    // Calculates the real-world distance (in km) between two
    // GPS coordinates. Much more accurate than a flat bounding
    // box because it accounts for the curve of the Earth.
    //
    // lat1, lng1 = user's location
    // lat2, lng2 = stall's location
    // -------------------------------------------------------
    public double calculateDistance(double lat1, double lng1,
                                    double lat2, double lng2) {

        // Convert degrees to radians (Math functions need radians)
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // Difference between the two latitudes and longitudes
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        // Core Haversine calculation
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Final distance in kilometers
        return EARTH_RADIUS_KM * c;
    }

    // -------------------------------------------------------
    // Format distance for display in the frontend
    // Returns "0.4 km" or "850 m" depending on the distance
    // -------------------------------------------------------
    public String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Convert to meters and round to nearest 50
            int meters = (int) Math.round(distanceKm * 1000 / 50.0) * 50;
            return meters + " m";
        } else {
            // Round to 1 decimal place
            return String.format("%.1f km", distanceKm);
        }
    }

    // -------------------------------------------------------
    // Check if a location is within a given radius (in km)
    // Used to filter out stalls that are too far away
    // -------------------------------------------------------
    public boolean isWithinRadius(double userLat, double userLng,
                                   double targetLat, double targetLng,
                                   double radiusKm) {
        double distance = calculateDistance(userLat, userLng, targetLat, targetLng);
        return distance <= radiusKm;
    }

    // -------------------------------------------------------
    // Convert km radius to degrees (for the bounding box query)
    // Used when passing latRange/lngRange to the repository
    // 1 degree of latitude ≈ 111 km (constant everywhere)
    // 1 degree of longitude ≈ 111 km * cos(latitude) (varies)
    // -------------------------------------------------------
    public double kmToLatDegrees(double km) {
        return km / 111.0;
    }

    public double kmToLngDegrees(double km, double latitude) {
        return km / (111.0 * Math.cos(Math.toRadians(latitude)));
    }
}