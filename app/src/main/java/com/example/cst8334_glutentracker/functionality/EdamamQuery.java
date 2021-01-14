package com.example.cst8334_glutentracker.functionality;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;

import com.example.cst8334_glutentracker.activity.CartActivity;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Product;

/**
 * Class that will be performing API queries to the Edamam database.
 *
 * @link https://developer.edamam.com/food-database-api
 */
public class EdamamQuery extends AsyncTask<String, Long, Product> {

    private Context context;
    boolean isGlutenFree;
    long upc;
    String appId = "90fb7f7d";
    String appKey = "ec9b27a10f3bb159f17fd932ac559526";
    String response;
    JSONObject jObject;
    String jProductLabel;
    Product prod;
    AlertDialog.Builder alertDialog;

    /**
     * Overloaded constructor that receives values from the ScanActivity class, will be used later
     * to perform API queries to Edamam.
     *
     * @param context This is used to pass the AlertDialog back to ScanActivity
     * @param upc Barcode value that will be used to query Edamam
     * @param isGluten Boolean passed from the checkbox, will be used to declare products gluten or gluten-free
     */
    public EdamamQuery(Context context, long upc, boolean isGluten){
        this.context = context;
        this.upc = upc;
        this.isGlutenFree = isGluten;
    }

    /**
     * Overridden method, attempts to perform an API query to Edamam. If it successfully finds
     * an item, it will parse the product name from the database which will be inserted in the product object
     * and finally will be added to the cart and database.
     */
    @Override
    public Product doInBackground(String... strings){
            String ret = null;
            String queryURL = "https://api.edamam.com/api/food-database/v2/parser?upc=" + upc + "&app_id=" + appId + "&app_key=" + appKey;

        /**
         * Attempt a API query to Edamam, if the connection is successful and an item is retrieved from the barcode sent, it will parse it, retrieve the product name and add it to the database (and cart if it's a gluten item)
         */
        try {
                URL url = new URL(queryURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                String result = sb.toString();
                jObject = new JSONObject(result);
                jProductLabel = jObject.getJSONArray("hints").getJSONObject(0).getJSONObject("food").getString("label");

                prod = new Product(upc, jProductLabel,"",1.00, isGlutenFree);
                GlutenDatabase db = new GlutenDatabase(context);
                db.insertIntoProductsTable(prod);
                if (prod.isGlutenFree()){
                    CartActivity.getProductsArrayList().add(prod);
                }
                ret = jProductLabel;
            } catch (MalformedURLException mfe) {
            prod = new Product(0, "Malformed URL exception",null,0, true);
        } catch (IOException ioe) {
            prod = new Product(0, "Internet not available or product not found",null,0, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prod;
    }

    /**
     * This method returns a product to the UI thread
     *
     * @param result Name of the product that is added to the database/cart
     */
    @Override
    protected void onPostExecute(Product result) {
        super.onPostExecute(result);
        switch (result.getProductName()) {
            case "Malformed URL exception":
                Toast.makeText(context, result.getProductName(), Toast.LENGTH_SHORT).show();
                break;
            case "Internet not available or product not found":
                Toast.makeText(context, result.getProductName(), Toast.LENGTH_SHORT).show();
                break;
            default:
                    Toast.makeText(context, "successfully retrieved " + result.getProductName(), Toast.LENGTH_LONG).show();
                    if (!result.isGlutenFree()){
                        /**
                         * Dialog listener will either add an item to the database and cart if the user clicks "Yes" or simply
                         * add the item to the database if the user click "No" (only relevant for gluten items)
                         */
                        DialogInterface.OnClickListener dialogInterfaceListener = (dialog, which) -> {

                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    CartActivity.getProductsArrayList().add(prod);
                                    Toast.makeText(context, "successfully added " + result.getProductName() + " to the cart", Toast.LENGTH_LONG).show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    Toast.makeText(context, result.getProductName() + " was not added to cart", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        };

                        alertDialog =  new AlertDialog.Builder(context);
                        alertDialog.setTitle("Add gluten item to cart")
                                .setMessage("Would you like to add this gluten item to the cart? By default, only gluten-free items are added.")
                                .setPositiveButton("Yes", dialogInterfaceListener)
                                .setNegativeButton("No", dialogInterfaceListener);
                        alertDialog.show();
                    }
                break;
        }
    }
}
