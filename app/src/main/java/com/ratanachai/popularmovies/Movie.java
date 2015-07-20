package com.ratanachai.popularmovies;

/**
 * Created by keng on 15/07/20.
 */
public class Movie {
    private String title;
    private String posterUrl;
    private String overview;
    private String userRating;
    private String releaseDate;

    //Take Json string and create an instance
    public Movie(String title, String poster, String overview, String userRating, String releaseDate){
        this.title = title;
        this.posterUrl = poster;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }
    public String getPosterUrl(){
        return posterUrl;
    }

}
