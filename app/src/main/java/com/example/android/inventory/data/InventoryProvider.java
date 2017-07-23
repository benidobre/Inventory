package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by bdobre on 16/07/2017.
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();


    private static final int ITEMS = 100;
    private static final int ITEM_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_WITH_ID);

        return uriMatcher;
    }

    private InventoryDbHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_WITH_ID:

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        if(TextUtils.isEmpty(name)){
            throw new IllegalArgumentException("Item requires a name");
        }
        String imageUri = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI);
        if (TextUtils.isEmpty(imageUri)) {
            throw new IllegalArgumentException("Item requires an image");
        }
        int price = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        int quantity = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
        if( quantity < 0 || price < 0){
            throw new IllegalArgumentException("Price and quantity can't be below 0");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_WITH_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_WITH_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }    }

    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if(contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        if(contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI)) {
            String imageUri = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI);
            if (TextUtils.isEmpty(imageUri)) {
                throw new IllegalArgumentException("Item requires an image");
            }
        }
        if(contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            int price = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if(  price < 0){
                throw new IllegalArgumentException("Price can't be below 0");
            }
        }
        if(contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            int quantity = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity < 0 ) {
                throw new IllegalArgumentException("Quantity can't be below 0");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
