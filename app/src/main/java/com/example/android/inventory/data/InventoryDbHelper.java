package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bdobre on 16/07/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
