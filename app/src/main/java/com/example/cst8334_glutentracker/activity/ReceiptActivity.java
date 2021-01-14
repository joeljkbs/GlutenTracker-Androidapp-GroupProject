package com.example.cst8334_glutentracker.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.Receipt;
import com.example.cst8334_glutentracker.functionality.ImageReceipt;

import java.util.ArrayList;
import java.util.List;

public class ReceiptActivity extends AppCompatActivity {
    /**
     * ArrayList that stores all receipt objects
     */
    ArrayList<Receipt> receipt;

    /**
     * Listview to display the receipts
     */
    ListView receiptList;
    /**
     * An instance of the ReceiptAdapter inner class used to populate the ListView
     */
    private static ReceiptAdapter adapter;
    private SQLiteDatabase database;

    /**
     * An instance of the GlutenDatabase class. This will be used to load receipts from the Receipt table.
     */
    private GlutenDatabase dbOpener = new GlutenDatabase(this);

    /**
     * Toolbar to navigate between different activities
     */
    Toolbar receiptTbar;

    /**
     * This method is called when the page is first loaded
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        receiptList=(ListView)findViewById(R.id.receiptList);// initializing the listview
        receipt= new ArrayList<>(); // initializing the arraylist to store receipts

        receiptTbar = (Toolbar)findViewById(R.id.receiptToolbar);// initializing the toolbar


        readFromDatabase();// to read any data from the database
        adapter=new ReceiptAdapter(receipt,this);// the adapter is instantiated
        receiptList.setAdapter(adapter);// to set the instance of ReceiptAdapter in the listview

        receiptList.setOnItemLongClickListener((adapterView, view, i, l) -> {
            //alertdialog to delete a receipt from the list
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(adapterView.getContext());
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){

                    case DialogInterface.BUTTON_POSITIVE:
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Long d=receipt.get(i).getId();// to get the id of the receipt to be deleted
                        dbOpener.deleteReceiptByID(d);// to delete the receipt from the database
                        receipt.remove(i);// to delete the receipt object from the arraylist
                        adapter.notifyDataSetChanged();// to refresh the listview
                        break;
                }
            };
            alertDialog.setTitle("Delete Receipt");
            alertDialog.setMessage("Do you want to delete this receipt");
            alertDialog.setNegativeButton("Delete", dialogClickListener);
            alertDialog.setPositiveButton("No", dialogClickListener);
            alertDialog.show();
            return true;
        });
        //to open a digital receipt based on the receipt clicked by the user
        receiptList.setOnItemClickListener((list,item,position,id)->{
            Intent intent= new Intent(ReceiptActivity.this,DigitalReceipt.class);

                DigitalReceipt.setPassedIndex(receipt.get(position).getId());
            startActivity(intent);
            });
        adapter.notifyDataSetChanged();
    }

    /**
     * To read all receipts from the database
     */
    private void readFromDatabase(){
        //List<Receipt> rec= dbOpener.selectAllReceipt(); original
        List<Receipt> rec = dbOpener.selectAllReceiptWithImage();
        if(rec!=null)
            receipt.addAll(rec);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.receiptButton).setVisible(false);
        menu.findItem(R.id.search_view).setVisible(false);
        return true;
    }

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
     * Inner class used to set a custom adapter to the listview
     */
    public class ReceiptAdapter extends ArrayAdapter<Receipt> {
        private ArrayList<Receipt> rData;

        Context mContext;
        TextView id;// to display the receipt id
        TextView img;// to the display the file path for image
        TextView amt;// to display the claimable amount
        TextView dte;//to display the date
        Button edit;// to edit the quantity or price
        ImageButton receiptImage; // to display an image of the receipt

        public ReceiptAdapter(ArrayList<Receipt> data, Context context)  {
            super(context,R.layout.receipt_layout,data);
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
            Receipt rS = getItem(position);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.receipt_layout, parent, false);
                id= (TextView) convertView.findViewById(R.id.rid);
                amt = (TextView) convertView.findViewById(R.id.deduction);
                dte = (TextView) convertView.findViewById(R.id.summarydate);
                receiptImage = convertView.findViewById(R.id.receiptImage);
                edit=convertView.findViewById(R.id.edit);

                id.setText(getString(R.string.receipt_id) + Long.toString(rS.getId()));//setting the receipt id
                amt.setText(getString(R.string.claim_amount) + Double.toString(rS.getTaxDeductionTotal()));//setting the claimable amount
                dte.setText(getString(R.string.date) + rS.getDate());//setting the date of transaction
                receiptImage.setImageBitmap(rS.getImage());

                receiptImage.setOnClickListener((v) ->{
                    Intent intent = new Intent(ReceiptActivity.this, ImageReceipt.class);
                    intent.putExtra("ReceiptId", rS.getId());
                    startActivity(intent);
                });
            return convertView;
        }
    }
}