const API = "http://localhost:8080/search";

const siteIcons = {
    "Amazon": "images/Amazon-logo-meaning.jpg",
    "Flipkart": "images/Flipkart_logo_(2026).svg.png",
    "Meesho": "images/Meesho_logo.png"
};

async function searchproduct() {
    const query = document.getElementById("searchInput").value.trim();
    const results = document.getElementById("results");

    if (!query) {
        alert("Please enter a product name!");
        return;
    }

    results.innerHTML = "<p>Searching...</p>";

    try {
        const response = await fetch(`${API}?query=${encodeURIComponent(query)}`);
        const data = await response.json();

        if (data.length > 0) {
            let cards = "";
            data.forEach((item) => {
                cards += `
                    <a href="${item.searchUrl}" target="_blank" class="result-card">
                       <div class="site-icon">
    <img src="${siteIcons[item.siteName] || 'images/default.jpg'}" alt="${item.siteName}" width="60">
</div>
                        <div class="site-name">${item.siteName}</div>
                        <div class="site-link">Click to view results →</div>
                    </a>
                `;
            });

            results.innerHTML = `<div class="results-grid">${cards}</div>`;
        } else {
            results.innerHTML = "<p>No results found</p>";
        }

    } catch (error) {
        results.innerHTML = "<p>Error connecting to server. Make sure Spring Boot is running!</p>";
    }
}

// Search on Enter key press
document.getElementById("searchInput").addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        searchproduct();
    }
});



