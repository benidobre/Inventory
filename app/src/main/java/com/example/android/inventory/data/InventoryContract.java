package com.example.android.inventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by bdobre on 16/07/2017.
 */

public final class InventoryContract {

    private InventoryContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";



    public static final class InventoryEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);


        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_IMG_URI = "uri";
    }
}
