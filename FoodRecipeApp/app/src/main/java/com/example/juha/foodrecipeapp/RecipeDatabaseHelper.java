package com.example.juha.foodrecipeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FoodRecipes.db";
    private static final String DATABASE_TABLE_NAME = "recipes";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_SOURCE_URL = "source_url";
    private static final String COLUMN_INGREDIENTS = "ingredients";

    private static final String JSON_ARRAY_INGREDIENTS = "ingredients";

    RecipeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + DATABASE_TABLE_NAME +
                " (" + COLUMN_ID + " TEXT PRIMARY KEY, " + COLUMN_TITLE + " TEXT, " + COLUMN_IMAGE_URL + " TEXT, " + COLUMN_SOURCE_URL + " TEXT, " + COLUMN_INGREDIENTS + " TEXT )";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME);
        onCreate(db);
    }

    public ArrayList<Recipe> getRecipes() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + DATABASE_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Recipe> recipes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String imageURL = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
                String sourceURL = cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_URL));
                String ingredientsString = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENTS));
                JSONObject jsonObj;
                ArrayList<String> ingredients = new ArrayList<>();
                try {
                    jsonObj = new JSONObject(ingredientsString);
                    JSONArray ingredientsJSONArray = jsonObj.getJSONArray(JSON_ARRAY_INGREDIENTS);
                    for (int i = 0; i < ingredientsJSONArray.length(); i++) {
                        String ingredient = ingredientsJSONArray.getString(i);
                        ingredients.add(ingredient);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Recipe recipe = new Recipe(id, title, imageURL, sourceURL, ingredients);
                recipes.add(recipe);
            } while (cursor.moveToNext());
        }
        db.close();
        return recipes;
    }

    public void insertRecipe(Recipe recipe) {
        ContentValues contentValues = new ContentValues();
        if (getRecipe(recipe.id) == null) {
            SQLiteDatabase db = this.getWritableDatabase();
            contentValues.put(COLUMN_ID, recipe.id);
            contentValues.put(COLUMN_TITLE, recipe.title);
            contentValues.put(COLUMN_IMAGE_URL, recipe.imageURL);
            contentValues.put(COLUMN_SOURCE_URL, recipe.sourceURL);
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(JSON_ARRAY_INGREDIENTS, new JSONArray(recipe.ingredients));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String ingredientsString = jsonObj.toString();
            contentValues.put(COLUMN_INGREDIENTS, ingredientsString);
            db.insert(DATABASE_TABLE_NAME, null, contentValues);
            db.close();
        }
    }

    public void updateRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, recipe.title);
        contentValues.put(COLUMN_IMAGE_URL, recipe.imageURL);
        contentValues.put(COLUMN_SOURCE_URL, recipe.sourceURL);
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(JSON_ARRAY_INGREDIENTS, new JSONArray(recipe.ingredients));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String ingredientsString = jsonObj.toString();
        contentValues.put(COLUMN_INGREDIENTS, ingredientsString);
        db.update(DATABASE_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{recipe.id});
        db.close();
    }

    public void deleteRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE_NAME, COLUMN_ID + "=?", new String[]{recipe.id});
        db.close();
    }

    public void deleteRecipe(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE_NAME, COLUMN_ID + "=?", new String[]{id});
        db.close();
    }

    public Recipe getRecipe(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_NAME + " WHERE " + COLUMN_ID + "=?", new String[]{id});
        Recipe recipe = null;
        if (cursor.moveToFirst() == false)
            return null;
        String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        String imageURL = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
        String sourceURL = cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_URL));
        String ingredientsString = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENTS));
        JSONObject jsonObj;
        ArrayList<String> ingredients = new ArrayList<>();
        try {
            jsonObj = new JSONObject(ingredientsString);
            JSONArray ingredientsJSONArray = jsonObj.getJSONArray(JSON_ARRAY_INGREDIENTS);
            for (int i = 0; i < ingredientsJSONArray.length(); i++) {
                String ingredient = ingredientsJSONArray.getString(i);
                ingredients.add(ingredient);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recipe = new Recipe(id, title, imageURL, sourceURL, ingredients);
        db.close();
        return recipe;
    }

}
