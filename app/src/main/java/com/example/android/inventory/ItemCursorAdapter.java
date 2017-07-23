package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.File;

/**
 * Created by bdobre on 16/07/2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c){super(context,c,0);}
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.item_name);
        TextView priceTextView = view.findViewById(R.id.item_price);
        TextView quantityTextView = view.findViewById(R.id.item_quantity);
        ImageView itemImage = view.findViewById(R.id.item_image);
        Button itemButton = view.findViewById(R.id.item_button);

        final int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        view.setTag(id);





        String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME));
        String uriString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI));
        //Uri imgUri = Uri.parse(uriString);
        int price = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));

        nameTextView.setText(name);
        priceTextView.setText(price+context.getString(R.string.price_currency));
        quantityTextView.setText(quantity+context.getString(R.string.pieces_abreviaton));


        //context.getContentResolver().takePersistableUriPermission(imgUri,0);

        File imgFile = new File(uriString);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());



            itemImage.setImageBitmap(myBitmap);

        }
        //itemImage.setImageURI(imgUri);

        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(quantity == 0){
                    Toast.makeText(context, "Can you sale nothing?", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues cv = new ContentValues();
                cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,quantity-1);

                context.getContentResolver().update(ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI,id),cv,null,null);
            }
        });

    }
}
