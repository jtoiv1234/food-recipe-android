package com.example.juha.foodrecipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class recipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(recipeDetailFragment.RECIPE_ID, getIntent().getStringExtra(recipeDetailFragment.RECIPE_ID));
            if (getIntent().getExtras().containsKey(recipeDetailFragment.SAVED_TO_DATABASE)) {
                arguments.putBoolean(recipeDetailFragment.SAVED_TO_DATABASE, getIntent().getBooleanExtra(recipeDetailFragment.SAVED_TO_DATABASE, false));
            }
            recipeDetailFragment fragment = new recipeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, recipeListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
