package com.example.cst8334_glutentracker.functionality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Receipt;

/**
 * This class is used to display the receipt image to the screen
 */
public class ImageReceipt extends AppCompatActivity {

    /**
     * This method is called when the activity is first opened
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_receipt);
        Intent fromActivity = getIntent();
        long receiptId = fromActivity.getLongExtra("ReceiptId", 0);
        GlutenDatabase dbOpener = new GlutenDatabase(this);
        Receipt receipt = dbOpener.selectReceiptByIDWithImage(receiptId);
        ImageView receiptImage = findViewById(R.id.receiptImageActivity);
        receiptImage.setImageBitmap(receipt.getImage());
    }
}