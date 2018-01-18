package com.example.juha.foodrecipeapp;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import android.net.Uri;
import android.text.TextUtils;

public class RecipesProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.juha.foodrecipeapp.RecipesProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/recipes";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String ID = "id";
    static final String TITLE = "title";
    static final String IMAGE_URL = "image_url";
    static final String SOURCE_URL = "source_url";
    static final String INGREDIENTS = "ingredients";

    private static HashMap<String, String> RECIPES_PROJECTION_MAP;

    static final int RECIPES = 1;
    static final int RECIPE_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "recipes", RECIPES);
        uriMatcher.addURI(PROVIDER_NAME, "recipes/#", RECIPE_ID);
    }

    /**
     * Database specific constant declarations
     */

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "food_recipes";
    static final String RECIPES_TABLE_NAME = "recipes";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + RECIPES_TABLE_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " title TEXT NOT NULL, " +
                    " image_url TEXT NOT NULL, " +
                    " source_url TEXT NOT NULL, " +
                    " ingredients TEXT NOT NULL);";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + RECIPES_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(RECIPES_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(RECIPES_TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case RECIPES:
                qb.setProjectionMap(RECIPES_PROJECTION_MAP);
                break;

            case RECIPE_ID:
                qb.appendWhere( ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }
        if (sortOrder == null || sortOrder == ""){
            sortOrder = TITLE;
        }

        Cursor c = qb.query(db,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case RECIPES:
                count = db.delete(RECIPES_TABLE_NAME, selection, selectionArgs);
                break;
            case RECIPE_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(RECIPES_TABLE_NAME, ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case RECIPES:
                count = db.update(RECIPES_TABLE_NAME, values, selection, selectionArgs);
                break;

            case RECIPE_ID:
                count = db.update(RECIPES_TABLE_NAME, values,
                        ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all recipes
             */
            case RECIPES:
                return "vnd.android.cursor.dir/vnd.example.recipes";
            /**
             * Get a recipe
             */
            case RECIPE_ID:
                return "vnd.android.cursor.item/vnd.example.recipes";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}