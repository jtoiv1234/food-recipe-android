package com.example.juha.foodrecipeapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class recipeDetailFragment extends Fragment implements OnTaskComplete {

    public static final String RECIPE_ID = "recipe_id";
    public static final String SAVED_TO_DATABASE = "saved_database";

    private String recipeId;
    private Recipe recipe;

    private Button buttonFavouriteRecipe;
    private CollapsingToolbarLayout appBarLayout;
    private ImageView imageViewRecipeImage;
    private TextView textViewRecipeTitle;
    private TextView textViewRecipeSourceURL;
    private TextView textViewRecipeIngredients;

    private boolean isRecipeSavedToFabourites;
    private boolean getRecipeFromDatabase;

    public recipeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(RECIPE_ID)) {
            recipeId = getArguments().getString(RECIPE_ID);
            if (getArguments().containsKey(SAVED_TO_DATABASE)) {
                getRecipeFromDatabase = true;
            }
            appBarLayout = null;
            //appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_detail, container, false);
        textViewRecipeTitle = (TextView) rootView.findViewById(R.id.recipe_detail_title);
        textViewRecipeSourceURL = (TextView) rootView.findViewById(R.id.recipe_detail_source);
        textViewRecipeIngredients = (TextView) rootView.findViewById(R.id.recipe_detail_ingredients);
        imageViewRecipeImage = (ImageView) rootView.findViewById(R.id.recipe_detail_image);
        buttonFavouriteRecipe = (Button) rootView.findViewById(R.id.recipe_detail_button_add_to_favourites);
        buttonFavouriteRecipe.setEnabled(false);
        buttonFavouriteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecipeSavedToFabourites) {
                    RemoveRecipeTask removeRecipeTask = new RemoveRecipeTask();
                    removeRecipeTask.execute();
                } else {
                    SaveRecipeTask saveRecipeTask = new SaveRecipeTask();
                    saveRecipeTask.execute();
                }
            }
        });
        IsRecipeIsSavedToDatabase checkIfRecipeIsSavedToDatabase = new IsRecipeIsSavedToDatabase();
        checkIfRecipeIsSavedToDatabase.execute();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getRecipeFromDatabase) {
            FetchRecipeFromDatabaseTask fetchRecipeFromDatabaseTask = new FetchRecipeFromDatabaseTask();
            fetchRecipeFromDatabaseTask.execute();
        } else {
            Uri.Builder builder = new Uri.Builder();
            String apiKey = getString(R.string.api_key);
            builder.scheme("http")
                    .authority("food2fork.com")
                    .appendPath("api")
                    .appendPath("get")
                    .appendQueryParameter("key", apiKey)
                    .appendQueryParameter("rId", recipeId);
            FetchJSONTask fetchJSONTask = new FetchJSONTask(builder, this, FetchJSONTask.RequestMethod.GET);
            fetchJSONTask.execute();
        }
    }

    @Override
    public void OnTaskComplete(String response) {
        if (response != null) {
            try {
                JSONObject responseJSONObj = new JSONObject(response);
                JSONObject recipeJSONObj = responseJSONObj.getJSONObject("recipe");
                String title = "";
                String imageURL = "";
                String sourceURL = "";
                ArrayList<String> ingredients = new ArrayList<>();
                if (recipeJSONObj.has("title")) {
                    title = recipeJSONObj.getString("title");
                }
                if (recipeJSONObj.has("image_url")) {
                    imageURL = recipeJSONObj.getString("image_url");
                }
                if (recipeJSONObj.has("source_url")) {
                    sourceURL = recipeJSONObj.getString("source_url");
                }
                if (recipeJSONObj.has("ingredients")) {
                    JSONArray jsonArrayIngredients = recipeJSONObj.getJSONArray("ingredients");
                    for (int i = 0; i < jsonArrayIngredients.length(); i++) {
                        String ingredient = jsonArrayIngredients.getString(i);
                        ingredients.add(ingredient);
                    }
                }
                String ingredientsAsString = "";
                for (int i = 0; i < ingredients.size(); i++) {
                    String ingredient = ingredients.get(i);
                    ingredientsAsString = ingredientsAsString + "\u2022 " + ingredient + "\n";
                }
                Picasso.with(getContext())
                        .load(imageURL)
                        .placeholder(R.drawable.ic_place_holder)
                        .error(R.mipmap.ic_error_text)
                        .into(imageViewRecipeImage);
                recipe = new Recipe(recipeId, title, imageURL, sourceURL, ingredients);
                textViewRecipeSourceURL.setText(sourceURL);
                textViewRecipeIngredients.setText(ingredientsAsString);
                textViewRecipeTitle.setText(title);
                if (appBarLayout != null) {
                    appBarLayout.setTitle(title);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class SaveRecipeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            RecipeDatabaseHelper recipeDatabaseHelper = new RecipeDatabaseHelper(getContext());
            recipeDatabaseHelper.insertRecipe(recipe);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isRecipeSavedToFabourites = true;
            buttonFavouriteRecipe.setText(getString(R.string.remove_from_favourites));
        }
    }

    private class RemoveRecipeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            RecipeDatabaseHelper recipeDatabaseHelper = new RecipeDatabaseHelper(getContext());
            recipeDatabaseHelper.deleteRecipe(recipeId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isRecipeSavedToFabourites = false;
            buttonFavouriteRecipe.setText(getString(R.string.add_to_favourites));
        }
    }

    private class FetchRecipeFromDatabaseTask extends AsyncTask<Void, Void, Recipe> {

        @Override
        protected Recipe doInBackground(Void... voids) {
            RecipeDatabaseHelper recipeDatabaseHelper = new RecipeDatabaseHelper(getContext());
            return recipeDatabaseHelper.getRecipe(recipeId);
        }

        @Override
        protected void onPostExecute(Recipe savedRecipe) {
            super.onPostExecute(savedRecipe);
            recipe = savedRecipe;
            Picasso.with(getContext())
                    .load(recipe.imageURL)
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.mipmap.ic_error_text)
                    .into(imageViewRecipeImage);
            textViewRecipeSourceURL.setText(recipe.sourceURL);
            String ingredientsAsString = "";
            for (int i = 0; i < recipe.ingredients.size(); i++) {
                String ingredient = recipe.ingredients.get(i);
                ingredientsAsString = ingredientsAsString + "\u2022 " + ingredient + "\n";
            }
            textViewRecipeIngredients.setText(ingredientsAsString);
            textViewRecipeTitle.setText(recipe.title);
            if (appBarLayout != null) {
                appBarLayout.setTitle(recipe.title);
            }
            isRecipeSavedToFabourites = true;
            buttonFavouriteRecipe.setText(getString(R.string.remove_from_favourites));
        }
    }

    private class IsRecipeIsSavedToDatabase extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            RecipeDatabaseHelper recipeDatabaseHelper = new RecipeDatabaseHelper(getContext());
            Recipe recipe = recipeDatabaseHelper.getRecipe(recipeId);
            if (recipe != null) {
                return new Boolean(true);
            }
            return new Boolean(false);
        }

        @Override
        protected void onPostExecute(Boolean isSavedToDatabase) {
            super.onPostExecute(isSavedToDatabase);
            isRecipeSavedToFabourites = isSavedToDatabase.booleanValue();
            if (isRecipeSavedToFabourites) {
                buttonFavouriteRecipe.setText(getString(R.string.remove_from_favourites));
            } else {
                buttonFavouriteRecipe.setText(getString(R.string.add_to_favourites));
            }
            buttonFavouriteRecipe.setEnabled(true);
        }
    }

}
