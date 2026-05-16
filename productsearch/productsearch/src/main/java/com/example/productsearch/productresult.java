package com.example.productsearch;


public class productresult {
    String siteName;
    String searchUrl;

    public productresult(String siteName, String searchUrl) {
        this.siteName = siteName;
        this.searchUrl = searchUrl;
    }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getSearchUrl() { return searchUrl; }
    public void setSearchUrl(String searchUrl) { this.searchUrl = searchUrl; }
}
