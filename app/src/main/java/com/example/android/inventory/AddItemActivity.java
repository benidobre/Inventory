package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.File;

public class AddItemActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private ImageView imagePreview;
    private Button addImageButton;
    private Uri imageUri;
    private String imagePath;

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        nameEditText = (EditText) findViewById(R.id.item_name_et);
        priceEditText = (EditText) findViewById(R.id.item_price_et);
        quantityEditText = (EditText) findViewById(R.id.item_quantity_et);
        imagePreview = (ImageView) findViewById(R.id.item_image_preview);
        addImageButton = (Button) findViewById(R.id.select_image);

        addImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.add_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePet();
                // Exit activity

                return true;


            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(AddItemActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        String name = nameEditText.getText().toString();
        String quantityString = quantityEditText.getText().toString();
        String priceString = priceEditText.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please fill in the name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(quantityString)){
            Toast.makeText(this, "Please fill in the quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(priceString)){
            Toast.makeText(this, "Please fill in the price", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(imagePath)){
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
            return;
        }


        int quantity = Integer.parseInt(quantityString);
        int price = Integer.parseInt(priceString);


        ContentValues cv = new ContentValues();
        cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,price);
        cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,quantity);
        cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,name);
        cv.put(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG_URI,imagePath);

        getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI,cv);

        finish();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver()
                    .query(selectedImage, filePathColumn, null, null,
                            null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            ImageView imageView = (ImageView) findViewById(R.id.item_image_preview);

            File imgFile = new File(imagePath);

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());



                imageView.setImageBitmap(myBitmap);

            }

        }
    }
}
