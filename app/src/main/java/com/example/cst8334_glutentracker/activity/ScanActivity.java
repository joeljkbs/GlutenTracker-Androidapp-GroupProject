/**
 * Copyright (c) 2017 Team Novus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.cst8334_glutentracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.example.cst8334_glutentracker.functionality.EdamamQuery;
import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Product;
import com.google.zxing.Result;

import static java.lang.Long.parseLong;

/**
 * This activity is used to display the layout for the barcode scanner activity in order to add items
 * to the shopping cart. Validation checks are performed as required to update the database and cart
 * as required.
 */
public class ScanActivity extends AppCompatActivity {
    Button acceptScannerButton;
    Button cancelScannerButton;
    EditText upcBarcode;
    CheckBox checkBox;
    GlutenDatabase dbOpener;
    SQLiteDatabase db;
    CodeScanner codeScanner;
    CodeScannerView scannerView;
    Product barcodeCheck;
    Toolbar scannerTbar;
    AlertDialog.Builder glutenToCartDialog;
    AlertDialog.Builder glutenDatabaseMismatchDialog;
    boolean cameraPermission;
    int CAMERA_PERMISSION_CODE;

    // Adding six second delay between scans, https://github.com/journeyapps/zxing-android-embedded/issues/59
    static final int DELAY = 6000;
    long delayTimeStamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scannerTbar = (Toolbar)findViewById(R.id.scannerToolbar);
        checkBox = (CheckBox) findViewById(R.id.glutenCheckBox);
        upcBarcode = (EditText) findViewById(R.id.barcodeEditText);
        acceptScannerButton = (Button) findViewById(R.id.acceptScannerButton);
        cancelScannerButton = (Button) findViewById(R.id.cancelScannerButton);
        dbOpener = new GlutenDatabase(this);
        db = dbOpener.getWritableDatabase();
        glutenToCartDialog = new AlertDialog.Builder(this);
        glutenDatabaseMismatchDialog = new AlertDialog.Builder(this);

        /**
         * Logic to request camera permissions from the user if not already granted.
         */
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermission = false;
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            cameraPermission = true;
        }

        scannerView = (CodeScannerView) findViewById(R.id.barcodeScanner);
        codeScanner = new CodeScanner(this, scannerView);
        codeScanner.getCamera();

        /**
         * Set the camera to continuously scan for barcodes
         */
        codeScanner.setScanMode(ScanMode.CONTINUOUS);

        /**
         * Enable all barcode formats to be suitable for API queries to Edamam
         */
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        /**
         * Code example taken from the following github repository:
         * @link https://github.com/yuriy-budiyev/code-scanner
         *
         * Enables the camera to constantly refresh and wait until a barcode image is found.
         * Once a barcode is found and decoded, send it to the runQuery() method and perform validation
         * checks.
         */
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                ScanActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * Logic to delay the time between scans. Currently set to 6 seconds
                         * as we currently have limitations with the current API (10 lookups per minute)
                         */
                        if (System.currentTimeMillis() - delayTimeStamp < DELAY){
                            return;
                        } else {
                            ScanActivity.this.runQuery(parseLong(result.getText()), checkBox.isChecked());
                            delayTimeStamp = System.currentTimeMillis();
                        }
                    }
                });
            }
        });

        /**
         * Logic for the accept button, pass the barcode from the EditText field and
         * CheckBox if pressed.
         */
        if (acceptScannerButton != null) {
            acceptScannerButton.setOnClickListener(acceptClick -> {
                if (upcBarcode.getText().toString().length() > 0) {
                    this.runQuery(getUPCEditText(), checkBox.isChecked());
                }
            });
        }

        /**
         * Logic for the cancel button, return to previous activity if pressed.
         */
        if (cancelScannerButton != null) {
            cancelScannerButton.setOnClickListener(cancelClick -> {
                finish();
            });
        }

        /**
         * Logic to enable/disable the flashlight
         */
        if (codeScanner.isFlashEnabled()) {
            codeScanner.setFlashEnabled(true);
        } else {
            codeScanner.setFlashEnabled(false);
        }
    }

    /**
     Performs verification checks and performs the necessary actions:
        1. If the item is in the cart, do not perform database or API query to Edamam.
        2. If the item is not in the cart but it exists in the database, perform the following:
            A) Retrieve the item from the database
            B) Ask the user if they want to add the item to the cart.
        3. If the item does not exist in the cart or database, perform an API query to Edamam and
            retrieve the item if it exists.

         @param upc - Barcode value retrieved from Edamam or the database
         @param isGlutenFree - parameter passed from the Check Box to declare if the item is gluten free or not

     */
    private void runQuery(long upc, boolean isGlutenFree) {
        boolean boolCartItem = false;
        db = dbOpener.getReadableDatabase();
        barcodeCheck = dbOpener.selectProductByID(upc);

        /**
         * Logic to confirm if the item has already been added to the cart. If it has,
         * simply display a message to the user.
         */
        boolCartItem = productInCart(upc);

        /**
         * Verify if the item was previously scanned (added to the database)
         */
        if (barcodeCheck !=null) {
            if (barcodeCheck.isGlutenFree() != isGlutenFree) {
                showGlutenMismatchDialog();
            } else if (barcodeCheck.isGlutenFree() == isGlutenFree) {
                if (boolCartItem == false) {
                    if (barcodeCheck.isGlutenFree()) {
                        CartActivity.getProductsArrayList().add(barcodeCheck);
                        Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " added to the cart", Toast.LENGTH_LONG).show();
                    } else if (!barcodeCheck.isGlutenFree()) {
                        showGlutenToCartDialog();
                    }
                } else if (boolCartItem == true) {
                    Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " already exists in the cart", Toast.LENGTH_LONG).show();
                }
            }
        /**
         * Perform API query to Edamam
         */
        } else if (barcodeCheck == null) {
            new EdamamQuery(ScanActivity.this, upc, isGlutenFree).execute();
            barcodeCheck = dbOpener.selectProductByID(upc);

            if (!isGlutenFree && barcodeCheck != null) {
                showGlutenToCartDialog();
            }
        }
    }

    /**
     *     Return Edit text field value
      */
    private long getUPCEditText() {
        return Long.valueOf(upcBarcode.getText().toString());
    }

    /**
     * Overwrite onResume method, used to display camera only if camera permissions
     * were accepted.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (cameraPermission){
            codeScanner.startPreview();
        }
    }

    /**
     * Method used to release resources used by CodeScanner when this method is called
     */
    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    /**
     * Method to hide the icons of the currently selected activity (ScanActivity.java)
     *
     * @param menu instance of the menu for the top toolbar
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.scannerButton).setVisible(false);
        menu.findItem(R.id.search_view).setVisible(false);
        return true;
    }

    /**
     * Method used to change activities when clicking/tapping on the toolbar icons
     *
     * @param item Item clicked/tapped on the main toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scannerButton:
                setResult(MainMenuActivity.RESULT_CODE_NAVIGATE_TO_SCANNER);
                finish();
                break;
            case R.id.cartButton:
                setResult(MainMenuActivity.RESULT_CODE_NAVIGATE_TO_CART);
                finish();
                break;
            case R.id.receiptButton:
                setResult(MainMenuActivity.RESULT_CODE_NAVIGATE_TO_RECEIPT);
                finish();
                break;
            case R.id.reportButton:
                setResult(MainMenuActivity.RESULT_CODE_NAVIGATE_TO_REPORT);
                finish();
                break;
        }
        return true;
    }

    /**
     *
     * @param alertDialog AlertDialog.Builder object that will be configured through this method
     * @param title Title that will be given at the top of the alert dialog box
     * @param message Message that will be given in the alert dialog box
     * @param listener Specifies the listener that will be used in order to perform specific actions
     * @return Returns the dialog box objects
     */
    public AlertDialog.Builder setAlertDialog(AlertDialog.Builder alertDialog, String title, String message, DialogInterface.OnClickListener listener) {
        return alertDialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", listener).setNegativeButton("No", listener);
    }

    /**
     * Method creating the message box to ask if users want to add the gluten item to the cart, calls the setAlertDialog() method
     */
    public void showGlutenToCartDialog() {
        setAlertDialog(glutenToCartDialog, "Add gluten item to cart", "Would you like to add this gluten item to the cart? By default, only gluten-free items are added.", glutenToCartListener);
        glutenToCartDialog.create().show();
    }

    /**
     * Method creating the message box when gluten item values mismatch, calls the setAlertDialog() method
     */
    public void showGlutenMismatchDialog() {
        setAlertDialog(glutenDatabaseMismatchDialog, "Mismatching gluten value", "The item previously retrieved does not match the selected item, would you like to change this?", glutenMismatchListener);
        glutenDatabaseMismatchDialog.show();
    }

    /**
     * Method used to change the gluten value of a product retrieved from the database. If the item exists in the cart, it will also update this item.
     */
    DialogInterface.OnClickListener glutenMismatchListener = (dialog, which) -> {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                dbOpener.getWritableDatabase();
                barcodeCheck.setIsGlutenFree(checkBox.isChecked());
                dbOpener.updateProductById(barcodeCheck);
                boolean itemInCart = false;
                int index;
                String glutenString = "";
                if (checkBox.isChecked()) {
                    glutenString = "gluten-free";
                } else {
                    glutenString = "gluten";
                }

                Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " successfully changed to " + glutenString, Toast.LENGTH_LONG).show();
                /**
                 * Verify if the item exists in the cart. If it does, update the item in the cart.
                 */
                if (CartActivity.getProductsArrayList().size() != 0){
                    for (Product prod : CartActivity.getProductsArrayList()) {
                        index = 0;
                        if (prod.getId() == barcodeCheck.getId()) {
                            itemInCart = true;
                            CartActivity.getProductsArrayList().set(index, barcodeCheck).setLinkedProduct(null);
                            break;
                        }
                        index++;
                    }
                }
                if (!barcodeCheck.isGlutenFree() && !productInCart(barcodeCheck.getId())) {
                    showGlutenToCartDialog();
                } else if (barcodeCheck.isGlutenFree() && !productInCart(barcodeCheck.getId())) {
                    CartActivity.getProductsArrayList().add(barcodeCheck);
                    Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " successfully added to cart ", Toast.LENGTH_LONG).show();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " was not changed", Toast.LENGTH_LONG).show();
                if (!barcodeCheck.isGlutenFree() && !productInCart(barcodeCheck.getId())) {
                    showGlutenToCartDialog();
                }
                break;
        }
    };

    /**
     * Listener to perform actions when users click yes/no for the glutenToCart alert dialog
     */
    DialogInterface.OnClickListener glutenToCartListener = (dialog, which) -> {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                CartActivity.getProductsArrayList().add(barcodeCheck);
                Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " successfully added to the cart", Toast.LENGTH_LONG).show();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Toast.makeText(ScanActivity.this, barcodeCheck.getProductName() + " was not added to the cart", Toast.LENGTH_LONG).show();
                break;
        }
    };

    /**
     * Method to verify if the item exists in the cart.
     * @param upc Barcode to compare in the cart
     * @return Return true if the product is found in the cart, return false if the item is not found.
     */
    public boolean productInCart(long upc) {
        if (CartActivity.getProductsArrayList().size() != 0){
            for (Product prod : CartActivity.getProductsArrayList()) {
                if (prod.getId() == upc) {
                    return true;
                }
            }
        }
        return false;
    };
    /**
     * Overwritten method used to enable/disable camera based on permissions selected
     * by the user.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraPermission = true;
            codeScanner.startPreview();
        } else {
            cameraPermission = false;
        }
    }
}