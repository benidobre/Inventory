package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.File;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri currentUri;
    private EditText changeAmountEditText;
    private TextView nameTextView;
    private TextView quantityTextView;
    private TextView priceTextView;
    private Button decreaseButton;
    private Button increaseButton;
    private int curentQuantity = 0;
    ImageView itemImage ;

    private static final int DETAIL_LOADER_ID = 3290;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        changeAmountEditText = (EditText) findViewById(R.id.increase_value_et);
        nameTextView = (TextView) findViewById(R.id.item_detail_name);
        quantityTextView = (TextView) findViewById(R.id.item_detail_quantity);
        priceTextView = (TextView) findViewById(R.id.item_detail_price);
        decreaseButton = (Button) findViewById(R.id.decrease_button);
        increaseButton = (Button) findViewById(R.id.increase_button);
        itemImage = (ImageView) findViewById(R.id.detail_image);

        changeAmountEditText.setText("1");

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curentQuantity == 0){
                    Toast.makeText(DetailActivity.this, "Can't go below 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                String amountString = changeAmountEditText.getText().toString();
                if(TextUtils.isEmpty(amountString)){
                    Toast.makeText(DetailActivity.this, "Please entry a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }
                int amount = Integer.parseInt(amountString);
                if(curentQuantity - amount < 0){
                    quantityTextView.setText("0"+getString(R.string.pieces_abreviaton));
                    curentQuantity = 0;
                    return;
                }
                curentQuantity -= amount;
                quantityTextView.setText(curentQuantity+getString(R.string.pieces_abreviaton));


            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountString = changeAmountEditText.getText().toString();
                if(TextUtils.isEmpty(amountString)){
                    Toast.makeText(DetailActivity.this, "Please entry a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }


                int amount = Integer.parseInt(amountString);
                if(amount + curentQuantity > 9999){
                    Toast.makeText(DetailActivity.this, "The supplier has no more stock, sorry", Toast.LENGTH_SHORT).show();
                    return;
                }
                curentQuantity += amount;
                quantityTextView.setText(curentQuantity+getString(R.string.pieces_abreviaton));

            }
        });

        currentUri = getIntent().getData();

        getLoaderManager().initLoader(DETAIL_LOADER_ID,null,this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                updateItem();
                // Exit activity
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order:
                order();
                finish();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void order() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this

        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.mail_subject)+ nameTextView.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_body));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if(currentUri != null) {
            getContentResolver().delete(currentUri,null,null);
        }
        finish();
    }

    private void updateItem() {


        ContentValues cv = new ContentValues();

        cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,curentQuantity);
        if(currentUri != null){
            getContentResolver().update(currentUri,cv,null,null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME));
            int price = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));
            String uriString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI));
            curentQuantity = quantity;




            nameTextView.setText(name);
            priceTextView.setText(price + getString(R.string.price_currency));
            quantityTextView.setText(quantity + getString(R.string.pieces_abreviaton));
            File imgFile = new File(uriString);

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());



                itemImage.setImageBitmap(myBitmap);

            }



        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
