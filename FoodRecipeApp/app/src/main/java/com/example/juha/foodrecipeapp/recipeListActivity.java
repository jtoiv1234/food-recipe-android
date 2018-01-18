package com.example.juha.foodrecipeapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class recipeListActivity extends AppCompatActivity {

    private final String SORT_BY_RATING = "r";
    private final String SORT_BY_TREND = "t";
    private String sortingMethod = SORT_BY_RATING;

    private boolean mTwoPane;

    private ArrayList<Recipe> recipes = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private RecipeRecyclerViewAdapter recipeRecyclerViewAdapter;
    private SearchView searchViewRecipes;
    private TextView emptyText;
    private TextView textViewFavouriteRecipesTitle;
    private ProgressBar progressBarRecipeList;

    private int page;
    private String searchTerms;
    private boolean isLoading;
    private boolean isLoadingNewQuery;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private int favouriteRecipesMenuId;
    private int findRecipesMenuId;
    private int selectedMenuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        isLoading = true;
        isLoadingNewQuery = false;
        searchTerms = "";
        page = 1;
        if (findViewById(R.id.recipe_detail_container) != null) {
            mTwoPane = true;
        }
        favouriteRecipesMenuId = R.menu.favourites_menu_list;
        findRecipesMenuId = R.menu.main_menu_list;
        selectedMenuId = findRecipesMenuId;
        progressBarRecipeList = (ProgressBar) findViewById(R.id.progressBar_list);
        emptyText = (TextView) findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) findViewById(R.id.recipe_list);
        searchViewRecipes = (SearchView) findViewById(R.id.searchView_toolbar_recipes);
        textViewFavouriteRecipesTitle = (TextView) findViewById(R.id.textView_toolbar_favourite_recipes);
        textViewFavouriteRecipesTitle.setVisibility(View.GONE);
        progressBarRecipeList.setVisibility(View.GONE);
        assert recyclerView != null;
        recipeRecyclerViewAdapter = new RecipeRecyclerViewAdapter();
        recyclerView.setAdapter(recipeRecyclerViewAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && isLoadingNewQuery == false) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();
                    if (isLoading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isLoading = false;
                            page = page + 1;
                            FetchRecipesTask fetchRecipesTask = new FetchRecipesTask();
                            fetchRecipesTask.execute();
                        }
                    }
                }
            }
        });

        searchViewRecipes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                getNewRecipesFromAPI();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    getNewRecipesFromAPI();
                    return true;
                }
                return false;
            }
        });
        getNewRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNewRecipes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(selectedMenuId, menu);
        int options = searchViewRecipes.getImeOptions();
        searchViewRecipes.setImeOptions(options| EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_popularity:
                searchViewRecipes.setEnabled(true);
                textViewFavouriteRecipesTitle.setVisibility(View.GONE);
                sortingMethod = SORT_BY_RATING;
                getNewRecipes();
                return true;
            case R.id.menu_item_trendiness:
                searchViewRecipes.setEnabled(true);
                textViewFavouriteRecipesTitle.setVisibility(View.GONE);
                sortingMethod = SORT_BY_TREND;
                getNewRecipes();
                return true;
            case R.id.menu_item_find_recipes:
                searchViewRecipes.setVisibility(View.VISIBLE);
                textViewFavouriteRecipesTitle.setVisibility(View.GONE);
                selectedMenuId = findRecipesMenuId;
                invalidateOptionsMenu();
                getNewRecipes();
                return true;
            case R.id.menu_item_favourites_recipes:
                searchViewRecipes.setVisibility(View.GONE);
                selectedMenuId = favouriteRecipesMenuId;
                textViewFavouriteRecipesTitle.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
                getNewRecipes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearRecipeListAndResetPage() {
        page = 1;
        recipes.clear();
        recipeRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void getNewRecipesFromAPI() {
        searchTerms = searchViewRecipes.getQuery().toString();
        clearRecipeListAndResetPage();
        FetchRecipesTask fetchRecipesTask = new FetchRecipesTask();
        fetchRecipesTask.execute();
    }

    private void getNewRecipesFromDatabase() {
        clearRecipeListAndResetPage();
        FetchSavedRecipes fetchSavedRecipes = new FetchSavedRecipes();
        fetchSavedRecipes.execute();
    }

    private void getNewRecipes() {
        if (isLoadingNewQuery == false) {
            isLoadingNewQuery = true;
            progressBarRecipeList.setVisibility(View.VISIBLE);
            if (selectedMenuId == findRecipesMenuId) {
                getNewRecipesFromAPI();
            }
            if (selectedMenuId == favouriteRecipesMenuId) {
                getNewRecipesFromDatabase();
            }
        }
    }

    private class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.textViewRecipeTitle.setText(recipes.get(position).title);
            Picasso.with(getApplicationContext())
                    .load(recipes.get(position).imageURL)
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.mipmap.ic_error_text)
                    .into(holder.imageViewRecipeImage);
            holder.itemView.setTag(recipes.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(recipeDetailFragment.RECIPE_ID, recipes.get(position).id);
                        if (selectedMenuId == favouriteRecipesMenuId) {
                            arguments.putBoolean(recipeDetailFragment.SAVED_TO_DATABASE, true);
                        }
                        recipeDetailFragment fragment = new recipeDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.recipe_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, recipeDetailActivity.class);
                        intent.putExtra(recipeDetailFragment.RECIPE_ID, recipes.get(position).id);
                        if (selectedMenuId == favouriteRecipesMenuId) {
                            intent.putExtra(recipeDetailFragment.SAVED_TO_DATABASE, true);
                        }
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textViewRecipeTitle;
            final ImageView imageViewRecipeImage;
            ViewHolder(View view) {
                super(view);
                textViewRecipeTitle = (TextView) view.findViewById(R.id.recipe_list_title);
                imageViewRecipeImage = (ImageView) view.findViewById(R.id.recipe_list_image);
            }
        }
    }

    private class FetchSavedRecipes extends AsyncTask<Void, Void, ArrayList<Recipe>> {

        @Override
        protected ArrayList<Recipe> doInBackground(Void... voids) {
            RecipeDatabaseHelper recipeDatabaseHelper = new RecipeDatabaseHelper(getApplicationContext());
            return recipeDatabaseHelper.getRecipes();
        }

        @Override
        protected void onPostExecute(ArrayList<Recipe> savedRecipes) {
            super.onPostExecute(savedRecipes);
            recipes.clear();
            recipeRecyclerViewAdapter.notifyDataSetChanged();
            for (int i = 0; i < savedRecipes.size(); i++) {
                recipes.add(savedRecipes.get(i));
                recipeRecyclerViewAdapter.notifyDataSetChanged();
            }
            isLoadingNewQuery = false;
            progressBarRecipeList.setVisibility(View.GONE);
        }
    }

    private class FetchRecipesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            Uri.Builder builder = new Uri.Builder();
            String apiKey = getString(R.string.api_key);
            builder.scheme("http")
                    .authority("food2fork.com")
                    .appendPath("api")
                    .appendPath("search")
                    .appendQueryParameter("key", apiKey)
                    .appendQueryParameter("sort", sortingMethod)
                    .appendQueryParameter("page", page + "");
            if (searchTerms.length() > 0) {
                searchTerms = searchTerms.replace(" ", "%20");
                builder.appendQueryParameter("q", searchTerms);
            }
            String apiURL = builder.build().toString();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(apiURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String responseJSON) {
            super.onPostExecute(responseJSON);
            isLoading = true;
            if (responseJSON != null) {
                try {
                    JSONObject responseJSONObj = new JSONObject(responseJSON);
                    int count = responseJSONObj.getInt("count");
                    if (count > 0) {
                        emptyText.setVisibility(View.INVISIBLE);
                    } else {
                        emptyText.setVisibility(View.VISIBLE);
                    }
                    JSONArray recipesJSONArray = responseJSONObj.getJSONArray("recipes");
                    recipes.clear();
                    recipeRecyclerViewAdapter.notifyDataSetChanged();
                    for (int i = 0; i < recipesJSONArray.length(); i++) {
                        JSONObject recipeJSONObj = recipesJSONArray.getJSONObject(i);
                        String id = "";
                        String title = "";
                        String imageURL = "";
                        String sourceURL = "";
                        if (recipeJSONObj.has("recipe_id")) {
                            id = recipeJSONObj.getString("recipe_id");
                        }
                        if (recipeJSONObj.has("title")) {
                            title = recipeJSONObj.getString("title");
                        }
                        if (recipeJSONObj.has("image_url")) {
                            imageURL = recipeJSONObj.getString("image_url");
                        }
                        if (recipeJSONObj.has("source_url")) {
                            sourceURL = recipeJSONObj.getString("source_url");
                        }
                        recipes.add(new Recipe(id, title, imageURL, sourceURL));
                        recipeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            isLoadingNewQuery = false;
            progressBarRecipeList.setVisibility(View.GONE);
        }
    }

}
