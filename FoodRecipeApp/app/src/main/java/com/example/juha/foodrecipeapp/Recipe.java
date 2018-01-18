package com.example.juha.foodrecipeapp;

import java.util.ArrayList;


public class Recipe {

    public final String id;
    public final String title;
    public final String imageURL;
    public final String sourceURL;
    public final ArrayList<String> ingredients;

    public Recipe(String id, String title, String imageURL, String sourceURL, ArrayList<String> ingredients) {
        this.id = id;
        this.title = title;
        this.imageURL = imageURL;
        this.sourceURL = sourceURL;
        this.ingredients = ingredients;
    }

    public Recipe(String id, String title, String imageURL, String sourceURL) {
        this.id = id;
        this.title = title;
        this.imageURL = imageURL;
        this.sourceURL = sourceURL;
        this.ingredients = new ArrayList<>();
    }

}
