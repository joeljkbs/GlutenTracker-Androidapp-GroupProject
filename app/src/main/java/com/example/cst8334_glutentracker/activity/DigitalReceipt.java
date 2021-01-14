package com.example.cst8334_glutentracker.activity;
/*

 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cst8334_glutentracker.functionality.CartListViewHolder;
import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Product;
import com.example.cst8334_glutentracker.entity.Receipt;

import java.util.ArrayList;

public class DigitalReceipt extends AppCompatActivity {
    /**
     * An instance of the ProductAdapter inner class used to populate the ListView
     */
    private ProductAdapter adapter;
    /**
     * An arraylist containing objects of the Product entity
     */
    private ArrayList<Product> products;
    /**
     * Listview to display the products in the receipt
     */
    private ListView productList;
    private SQLiteDatabase database;
    /**
     * An instance of the GlutenDatabase class. This will be used to load receipts from the Receipt table.
     */
    private GlutenDatabase dbOpener = new GlutenDatabase(this);
    /**
     * Textview to display Receipt Id
     */
    TextView rrid;
    /**
     * Textview to display date of purchase
     */
    TextView rdate;
    /**
     * Textview to display the amount user can claim
     */
    TextView ded;
    /**
     * To get the data for the respective receipt the user selects
     */
    Intent fromActivity;
    /**
     * To store the total amount of all the products purchased
     */
    TextView total;
    /**
     * To select the appropriate the receipt details from the database
     */
    public static long passedIndex;
    /**
     * To load receipt details from a receipt object
     */
    Receipt receipt;
    /**
     * To update the linked product in the receipt
     */
    static Product productToPass;
    /**
     * To get the index of a product to display it
     */
    static int index;

    public static long getPassedIndex() {
        return passedIndex;
    }

    /**
     * Setter for PassedIndex
     * @param index index of the product in the arraylist
     */
    public static void setPassedIndex(long index) {
        passedIndex = index;
    }

    /**
     * This method is called when the page is first loaded
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_receipt);

        fromActivity=getIntent();
        productList=findViewById(R.id.products);
        products=new ArrayList<>();
        rrid=findViewById(R.id.ridf);
        rdate=findViewById(R.id.datef);
        ded=findViewById(R.id.dedf);
        total=findViewById(R.id.ta);

        loadFromDatabase();
        rrid.setText(receipt.getId()+"");//setting the receipt id
        rdate.setText(receipt.getDate());//setting the date
        ded.setText(String.format("%.2f",receipt.getTaxDeductionTotal()));//setting the claimable amount
        total.setText(String.format("%.2f",receipt.getTotalPrice()));
        adapter=new ProductAdapter(products,this);// initializing the adapter object
        productList.setAdapter(adapter);//setting the products to the listview
        adapter.notifyDataSetChanged();// to refresh the listview
    }

    /**
     * To load all the product details from the database into the receipt
     */
    private void loadFromDatabase(){
        receipt=dbOpener.selectReceiptByID(passedIndex);
        products.addAll(receipt.getProducts());
    }
    /**
     * getter for productToPass
     * @return Product productToPass
     */
    public static Product getProductToPass() {
        return productToPass;
    }
    /**
     * setter for ProductToPass
     * @param productToPass
     */
    public static void setProductToPass(Product productToPass) {
        DigitalReceipt.productToPass = productToPass;
    }
    /**
     * getter for index
     * @return int index
     */
    public static int getIndex() {
        return index;
    }
    /**
     * setter for index
     * @param index
     */
    public static void setIndex(int index) {
        DigitalReceipt.index = index;
    }
    /**
     * Function invoked when the digitalreceipt activity/page is restarted
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(getProductToPass() != null){
            products.set(getIndex(),getProductToPass());
            setProductToPass(null);
            setIndex(0);
            adapter.notifyDataSetChanged();
        }
    }
    /**
     * Inner class used to set a custom adapter to the listview
     */
    public class ProductAdapter extends ArrayAdapter<Product> {
        private ArrayList<Product> rData;

        Context mContext;
        TextView proid;//to display the product id
        TextView name;//to display the product name
        TextView desc;// to display the product description
        TextView gluten;// to display the linked product name
        TextView quantity;// to display the purchased product quantity
        TextView dedp;// to display the deductible for the individual product
        Button edit;// to edit the quantity or price
        Button link;// to link gluten free products to gluten products

        public ProductAdapter(ArrayList<Product> data, Context context)  {
            super(context,R.layout.digital_receipt_layout,data);
            this.rData=data;
            this.mContext=context;
        }
        /**
         * Method to create a custom view
         * @param position position of the item in the list
         * @param convertView view of the list
         * @param parent
         * @return a view with custom items
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Product product = getItem(position);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.digital_receipt_layout, parent, false);
            name = (TextView) convertView.findViewById(R.id.pname);
            gluten = (TextView) convertView.findViewById(R.id.gluten);
            quantity = (TextView) convertView.findViewById(R.id.qty);
            dedp = (TextView) convertView.findViewById(R.id.prize);
            edit = convertView.findViewById(R.id.edit);
            link = convertView.findViewById(R.id.lnk);
            if(product.getLinkedProduct()==null)// to change the text of the link button to link/linked
                link.setText("Link");
            else
                link.setText("Linked");

            if(!product.isGlutenFree()) {// to disable the link button when the product is a gluten product
                link.setEnabled(false);
                link.setBackgroundResource(R.drawable.disable_btn_background);
            }

            Context context=convertView.getContext();

            name.setText(getString(R.string.product_name) + product.getProductName());// to set the product name
            if(product.getLinkedProduct()!=null)// to prevent an app from crashing, if there is no linked product
                gluten.setText(getString(R.string.linkedProductName) + product.getLinkedProduct().getProductName());// to set the linked product
            quantity.setText(getString(R.string.quantity) + Integer.toString(product.getQuantity()));// to set the quantity of the products purchased
            dedp.setText(getString(R.string.price) + product.getDisplayedPriceAsString());// to set the displayed price of a product


            edit.setOnClickListener((v) -> {
                Receipt oldReceipt = new Receipt(receipt.getId(), receipt.getProducts(), receipt.getReceiptFile(),
                    receipt.getTaxDeductionTotal(), receipt.getTotalPrice(), receipt.getDate(), receipt.getImage());
                View row = getLayoutInflater().inflate(R.layout.activity_edit_receipt, parent, false);
                CartListViewHolder.editProduct(DigitalReceipt.this,product,adapter,row,dbOpener,passedIndex);
                if(!oldReceipt.equals(receipt)){
                    double deduction= receipt.getTaxDeductionTotal();
                    deduction=0;
                    double totalAsDouble = receipt.getTotalPrice();
                    totalAsDouble = 0;
                    for(Product p: products){
                        deduction+=p.getDeduction();
                        totalAsDouble+=p.getDisplayedPrice();
                    }
                    receipt.setTaxDeductionTotal(deduction);// to set the deduction of the receipt entity
                    receipt.setTotalPrice(totalAsDouble);//to set the total of the receipt entity
                    dbOpener.updateReceiptById(receipt);// to update the receipt in the database
                    ded.setText(String.format("%.2f", deduction));// to set the claimable amount
                    total.setText(String.format("%.2f", totalAsDouble));
                }

            /*  double deduction= receipt.getTaxDeductionTotal();
              deduction=0;
              double totalAsDouble = receipt.getTotalPrice();
              totalAsDouble = 0;
              for(Product p: products){
                    deduction+=p.getDeduction();
                    totalAsDouble+=p.getDisplayedPrice();
                }
                receipt.setTaxDeductionTotal(deduction);// to set the deduction of the receipt entity
                receipt.setTotalPrice(totalAsDouble);//to set the total of the receipt entity
                dbOpener.updateReceiptById(receipt);// to update the receipt in the database
                ded.setText(String.format("%.2f", deduction));// to set the claimable amount
                total.setText(String.format("%.2f", totalAsDouble)); */
            });

            link.setOnClickListener((v) -> {
                Intent intent = new Intent(DigitalReceipt.this, Link.class);
                intent.putExtra("Index", position);
                setProductToPass(product);
                setIndex(position);
                Link.setPassedContext(DigitalReceipt.this);
                startActivity(intent);
            });
            return convertView;
        }
    }
}