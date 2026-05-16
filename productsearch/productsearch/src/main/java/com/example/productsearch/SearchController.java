package com.example.productsearch;

import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @GetMapping
    public List<productresult> searchProduct(@RequestParam String query) {
        List<productresult> results = new ArrayList<>();

        // Encode the search query for URL
        String encodedQuery = query.trim().replace(" ", "+");

         // Amazon
        results.add(new productresult(
            "Amazon",
            "https://www.amazon.in/s?k=" + encodedQuery
        ));

        // Flipkart
        results.add(new productresult(
            "Flipkart",
            "https://www.flipkart.com/search?q=" + encodedQuery
        ));

        // Meesho
        results.add(new productresult(
            "Meesho",
            "https://www.meesho.com/search?q=" + encodedQuery
        ));

        return results;

        
    }

    

    
}
