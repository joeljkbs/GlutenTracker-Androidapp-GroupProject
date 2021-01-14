package com.example.cst8334_glutentracker.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cst8334_glutentracker.functionality.CartListViewHolder;
import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class CartActivity extends AppCompatActivity {

    /**
     * The adapter used to show changes in the listview
     */
    private Adapter adapter = new Adapter();

    /**
     * This list contains a list of products in the cart
     */
    private static ArrayList<Product> productsArrayList = new ArrayList<>();;

    /**
     * This list contains a list of product IDs
     */
    private static List<Long> productIdList;

    /**
     * This is used to check how many products are in the cart
     */
    private static int productCount;

    /**
     * An instance of the GlutenDatabase class. This is used to perform operations to the database
     */
    private GlutenDatabase db = new GlutenDatabase(this);

    /**
     * The context of the activity
     */
    private Context context;

    /**
     * A TextView that shows the total deductible able to be claimed
     */
    private TextView totalDeductibleDisplay;

    /**
     * A TextView that shows the total the user is paying for
     */
    private TextView total;

    /**
     * The total the user is paying for as a double. This gets updated in the getView() method
     */
    private  double totalPaid = 0;

    /**
     * The total deductible as a double. This gets updated in the getView() method
     */
    private double totalDeductible = 0;

    /**
     * This is used to add toolbar functionality to the page
     */
    Toolbar cartTbar;

    /**
     * Used for taking a picture
     */
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * This method is called when the page is first loaded
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartTbar = (Toolbar)findViewById(R.id.cartToolbar);
        if(getProductsArrayList().isEmpty()) {
            SharedPreferences pre = getSharedPreferences("cart_activity", Context.MODE_PRIVATE);
            productIdList = new ArrayList<>();
            productCount = pre.getInt("Product count", 0);

            while (productCount > 0) {
                productIdList.add(pre.getLong(Integer.toString(productCount), 1));
                productCount--;
            }
            for (String key : pre.getAll().keySet()) {
                pre.edit().remove(key).apply();
            }
            for (long id : productIdList) {
                productsArrayList.add(db.selectProductByID(id));
            }
        }

        ListView purchases = findViewById(R.id.purchases);
        Button checkoutButton = findViewById(R.id.checkout_button);
        Button addNewProductButton = findViewById(R.id.addNewProductButton);
        addNewProductButton.setOnClickListener((v) -> {
            startActivity(new Intent(CartActivity.this, ScanActivity.class));
        });

        totalDeductibleDisplay = findViewById(R.id.totalDeductible);
        total = findViewById(R.id.total);
        purchases.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        checkoutButton.setOnClickListener((View v) -> {
            if(!productsArrayList.isEmpty()) {
                int numberOfNotLinkedProducts = 0;
                String message;
                for(Product product: getProductsArrayList()){
                    db.updateProductById(product);
                    if(product.getLinkedProduct() == null && product.isGlutenFree())
                        numberOfNotLinkedProducts++;
                }
                if(numberOfNotLinkedProducts > 1)
                    message = "There are " + numberOfNotLinkedProducts + " gluten-free products not linked. Do you still wish to continue checkout? "
                        + "This will clear your current cart and finalize your purchase.";
                else if(numberOfNotLinkedProducts == 1)
                    message = "There is " + numberOfNotLinkedProducts + " gluten-free product not linked. Do you still wish to continue checkout? "
                            + "This will clear your current cart and finalize your purchase.";
                else
                    message = "Are you sure you would like to checkout? This will clear your current cart.";
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                // DialogInterface learned from https://stackoverflow.com/questions/20494542/using-dialoginterfaces-onclick-method
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        takePicture();
                        break;
                }
            };
            alertDialog.setTitle("Finalize Purchase");
            alertDialog.setMessage(message);
            alertDialog.setPositiveButton("No", dialogClickListener);
            alertDialog.setNegativeButton("Yes", dialogClickListener);
            alertDialog.create().show();

            }
            else{
                Toast.makeText(this, "Cart is empty, unable to checkout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method allows the user to take a picture of the receipt upon purchase
     */
    private void takePicture(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * This method is used after the user takes a picture of the receipt. If the user has taken a picture,
     * it will make an alert dialog asking the user to confirm if this is the picture they want.
     * @param receiptImage
     */
    private void makeAlertDialog(Bitmap receiptImage){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ImageView receipt = new ImageView(this);
        receipt.setImageBitmap(receiptImage);
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    db.insertIntoProductsTable(productsArrayList);
                    db.insertIntoReceiptsTableWithImage(productsArrayList, "file", totalDeductible, totalPaid, new Date().toString(), receiptImage);
                    getProductsArrayList().clear();
                    totalDeductibleDisplay.setText("");
                    total.setText("");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Purchase finalized", Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        alertDialog.setTitle("Accept this picture?");
        alertDialog.setView(receipt);
        alertDialog.setPositiveButton("No", dialogClickListener);
        alertDialog.setNegativeButton("Yes", dialogClickListener);
        alertDialog.create().show();
    }

    /**
     * This method returns the arraylist of products
     * @return The arraylist of products
     */
    public static ArrayList<Product> getProductsArrayList(){
        return productsArrayList;
    }

    /**
     * This method is called when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    /**
     * This is used after the user takes a picture. If the requestcode passed is the same as the image capture, it will call the makeAlertDialog() method
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The data passed, this will be used to get the image
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            makeAlertDialog(image);
        }
    }

    /**
     * This method pauses the activity
     */
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pre = getSharedPreferences("cart_activity", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pre.edit();
        for(String key: pre.getAll().keySet()){
            pre.edit().remove(key).apply();
        }
        productCount = productsArrayList.size();
        for(int i = 0; i< productCount; i++){
            edit.putLong(Integer.toString(i), productsArrayList.get(i).getId());
        }
        edit.putInt("Product count", productCount);
        edit.apply();
        db.insertIntoProductsTable(productsArrayList);
    }

    /**
     * This method is used for the toolbar
     * @param menu The menu
     * @return True or false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.cartButton).setVisible(false);
        menu.findItem(R.id.search_view).setVisible(false);
        return true;
    }

    /**
     * This method is used to go to different activities when certain icons in the toolbar is clicked
     * @param item The menu item
     * @return True or false
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
     * This inner class is an adapter for the arraylist.
     */
    class Adapter extends BaseAdapter{

        /**
         * Counts the number of items in the arraylist
         * @return The number of items in the arraylist
         */
        @Override
        public int getCount() {
            return productsArrayList.size();
        }

        /**
         * This method gets the Product in the selected position
         * @param position The position of the arraylist
         * @return The found Product
         */
        @Override
        public Product getItem(int position) {
            return productsArrayList.get(position);
        }

        /**
         * This method returns the id (upc code) of the Product in the selected position
         * @param position The position of the arraylist
         * @return The product's upc code
         */
        @Override
        public long getItemId(int position) {
            return productsArrayList.get(position).getId();
        }

        /**
         * This method returns the view that populates a row. This view changes whether the Product is linked or not, and it changes depending on
         * if the product is gluten-free or not (only gluten-free items may be linked, otherwise the link button will be disabled).
         * @param position The position of the arraylist
         * @param convertView Allows for recycling of views when scrolling
         * @param parent The parent class.
         * @return The view for the row
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Product product = (Product) getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.activity_product_list, parent, false);
            context = newView.getContext();
            TextView isGluten = newView.findViewById(R.id.glutenFree);
            if(product.isGlutenFree())
                isGluten.setText(getString(R.string.product_type_gluten));
            else
                isGluten.setText(R.string.product_type_not_gluten);
            TextView deductibleText = newView.findViewById(R.id.deductibleText);
            TextView linkedProductName = newView.findViewById(R.id.linkedProductName);
            if(product.getLinkedProduct() != null) {
                deductibleText.setText(getString(R.string.deductible) + product.getDeductionAsString());
                linkedProductName.setText(getString(R.string.linkedProductName) + product.getLinkedProduct().getProductName());
            }
            else{
                deductibleText.setVisibility(View.INVISIBLE);
                linkedProductName.setVisibility(View.INVISIBLE);
            }

            TextView productName = newView.findViewById(R.id.productName);
            productName.setText(getString(R.string.product_name) + product.getProductName());

            TextView price = newView.findViewById(R.id.price);
            price.setText(getString(R.string.price) + product.getDisplayedPriceAsString());
            TextView quantity = newView.findViewById(R.id.quantity);
            quantity.setText(Integer.toString(product.getQuantity()));
            Button plusButton = newView.findViewById(R.id.plusButton);
            plusButton.setOnClickListener((v) ->{
                int convertedToInt = (Integer.parseInt(quantity.getText().toString())) +1;
                product.changeQuantityAndDisplayedPrice(convertedToInt);
                if(product.getLinkedProduct() != null){
                    deductibleText.setText(getString(R.string.deductible) + product.getDeductionAsString());
                }
                adapter.notifyDataSetChanged();
            });

            Button minusButton = newView.findViewById(R.id.minusButton);
            minusButton.setOnClickListener((v) ->{
                if(product.getQuantity() > 1) {
                    int convertedToInt = (Integer.parseInt(quantity.getText().toString())) - 1;
                    product.changeQuantityAndDisplayedPrice(convertedToInt);
                    if(product.getLinkedProduct() != null){
                        deductibleText.setText(getString(R.string.deductible) + product.getDeductionAsString());
                    }
                    adapter.notifyDataSetChanged();
                }
            });

           Button removeButton = newView.findViewById(R.id.removeFromCart);
            removeButton.setOnClickListener((v) -> {
                totalPaid = totalPaid - getProductsArrayList().get(position).getDisplayedPrice();
                totalDeductible = totalDeductible - getProductsArrayList().get(position).getDeduction();
                getProductsArrayList().remove(position);
                if(getProductsArrayList().isEmpty()){
                    total.setText(getString(R.string.total) + String.format("%.2f", 0.00));
                    totalDeductibleDisplay.setText(getString(R.string.total_deductible) + String.format("%.2f", 0.00));
                }
                adapter.notifyDataSetChanged();
            });

            Button linkButton = newView.findViewById(R.id.linkButton);
            if(product.getLinkedProduct() == null){
                linkButton.setText("Link Product");
            }
            else{
                linkButton.setText("Linked!");
            }
            if(!product.isGlutenFree()){
                linkButton.setEnabled(false);
                linkButton.setBackgroundResource(R.drawable.disable_btn_background);
            }
            linkButton.setOnClickListener((v) -> {
                 Intent intent = new Intent(CartActivity.this, Link.class);
                intent.putExtra("Index", position);
                Link.setPassedContext(CartActivity.this);
                startActivity(intent);
            });

            Button editButton = newView.findViewById(R.id.editProduct);
            editButton.setOnClickListener((v) -> {
                View row = getLayoutInflater().inflate(R.layout.activity_edit_receipt, parent, false);
                CartListViewHolder.editProduct(CartActivity.this,product,adapter,row,null,0);
            });

            double totalDeductibleAsDouble = 0;
            double totalAsDouble = 0;
            for(Product products: getProductsArrayList()){
                if(products.getLinkedProduct() != null){
                    totalDeductibleAsDouble += products.getDisplayedPrice() - products.getLinkedProduct().getDisplayedPrice();
                }
                totalAsDouble += products.getDisplayedPrice();
                total.setText(getString(R.string.total) + String.format("%.2f", totalAsDouble));
                totalPaid = totalAsDouble;
                totalDeductibleDisplay.setText(getString(R.string.total_deductible) + String.format("%.2f", totalDeductibleAsDouble));
                totalDeductible = totalDeductibleAsDouble;
            }
            return newView;
        }
    }


}