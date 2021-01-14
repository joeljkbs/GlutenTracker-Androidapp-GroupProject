package com.example.cst8334_glutentracker.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cst8334_glutentracker.functionality.CartListViewHolder;
import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class Link extends AppCompatActivity {

    /**
     * This is an arraylist that holds non-gluten free Products
     */
    private ArrayList<Product> listOfProducts = new ArrayList<Product>();

    /**
     * An instance of the FragmentAdapter inner class used to populate the ListView
     */
    FragmentAdapter adapter = new FragmentAdapter();

    /**
     * Gets the intent from the previous class
     */
    Intent fromActivity;

    /**
     * The index passed to this class. This is the position of the arraylist from the previous activity. This will be used to link a product to that specific product.
     */
    int passedIndex;
    String passedName;
    String linkedName;
    Context context;
    AlertDialog.Builder alertDialog;

    static Context passedContext;
    private SQLiteDatabase database;
    /**
     * An instance of the GlutenDatabase class. This will be used to load Products from the Products table that are not gluten-free.
     */
    private GlutenDatabase dbOpener = new GlutenDatabase(this);

    /**
     * This method is called when the page is first loaded
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        fromActivity = getIntent();
        passedIndex = fromActivity.getIntExtra("Index", 3);
        ListView linkTest = findViewById(R.id.linkTest);
        List<Product> nonGlutenFreeProducts = dbOpener.selectProductsByNonGlutenFree();
        if(nonGlutenFreeProducts != null) {
            for (Product p : nonGlutenFreeProducts)
                getNonGlutenArrayList().add(p);
        }
        linkTest.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static Context getPassedContext() {
        return passedContext;
    }

    public static void setPassedContext(Context passedContext) {
        Link.passedContext = passedContext;
    }

  public ArrayList<Product> getNonGlutenArrayList(){return listOfProducts;}

    class FragmentAdapter extends BaseAdapter {

        /**
         * Counts the number of items in the arraylist
         * @return The number of items in the arraylist
         */
        @Override
        public int getCount() {
            return listOfProducts.size();
        }

        /**
         * This method gets the Product in the selected position
         * @param position The position of the arraylist
         * @return The found Product
         */
        @Override
        public Product getItem(int position) {
            return listOfProducts.get(position);
        }

        /**
         * This method returns the id (upc code) of the Product in the selected position
         * @param position The position of the arraylist
         * @return The product's upc code
         */
        @Override
        public long getItemId(int position) {
            return listOfProducts.get(position).getId();
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
            if(getPassedContext() instanceof CartActivity)
                product.setQuantity(CartActivity.getProductsArrayList().get(passedIndex).getQuantity());
            if(getPassedContext() instanceof DigitalReceipt)
                product.setQuantity(DigitalReceipt.getProductToPass().getQuantity());
            product.setDisplayedPrice(product.getPrice() * product.getQuantity());
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.activity_fragment_populate_listview, parent, false);
            context = newView.getContext();
            TextView nameText = newView.findViewById(R.id.productFoundName);
            TextView priceText = newView.findViewById(R.id.productFoundPrice);
            nameText.setText(getString(R.string.product_name) + " " + product.getProductName());
            priceText.setText(getString(R.string.price) + product.getDisplayedPriceAsString());


            Button testButton = newView.findViewById(R.id.linkCommit);
            testButton.setOnClickListener((v) -> {

                DialogInterface.OnClickListener dialogInterfaceListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            if(getPassedContext() instanceof CartActivity)
                                CartActivity.getProductsArrayList().get(passedIndex).setLinkedProduct(product);
                            if(getPassedContext() instanceof DigitalReceipt)
                                DigitalReceipt.getProductToPass().setLinkedProduct(product);
                            adapter.notifyDataSetChanged();
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:

                            break;
                    }
                };

                //Check if product names are similar
                linkedName = nameText.getText().toString();
                if(getPassedContext() instanceof CartActivity)
                    passedName = CartActivity.getProductsArrayList().get(passedIndex).getProductName();
                if(getPassedContext() instanceof DigitalReceipt)
                    passedName = DigitalReceipt.getProductToPass().getProductName();
                //Split linkedName into an array containing each separate word
                String[] passedNameArray = passedName.split(" ");
                //Check if the linked name contains the last word from the passedNameArray
                if(linkedName.toLowerCase().contains(passedNameArray[passedNameArray.length - 1].toLowerCase())) {
                    CartActivity.getProductsArrayList().get(passedIndex).setLinkedProduct(product);
                    adapter.notifyDataSetChanged();
                    finish();
                } else {
                    // Create alert dialog
                    alertDialog = new AlertDialog.Builder(context).setTitle("Products may not be the same")
                            .setMessage("The products you are attempting to link can't be verified to be similar. " +
                                    "Are you sure you want to link them?")
                            .setPositiveButton("Yes", dialogInterfaceListener).setNegativeButton("No", dialogInterfaceListener);
                    alertDialog.create().show();
                }
            });

           Button editButton = newView.findViewById(R.id.editGlutenButton);
           editButton.setOnClickListener((v) -> {
               View row = getLayoutInflater().inflate(R.layout.activity_edit_receipt, parent, false);
               CartListViewHolder.editProduct(context, product, adapter, row,null,0);
           });
            return newView;
        }
    }
}