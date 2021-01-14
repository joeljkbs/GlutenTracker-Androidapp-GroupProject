/**
 * Project: cst83334 team 11 project
 * Name: Feng Sun
 * Student id: 040634005
 * Date: 2020-12-05
 */

package com.example.cst8334_glutentracker.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.ItemsModel;
import com.example.cst8334_glutentracker.entity.Product;
import com.example.cst8334_glutentracker.entity.Receipt;

import java.util.ArrayList;
import java.util.List;

/**
 * class ReportActivity for the receipt find
 */
public class ReportActivity extends AppCompatActivity {

    AlertDialog myDialog;
    TextView txtA;
    ListView lstPopup;

    ListView lstView;
    Button btnRpt;
    /**
     * define private value
     */
    private GlutenDatabase dbOpener = new GlutenDatabase(this);
    private String uId,uDate,uItem,uSub,uTax;
    private List<Product> uItems;



    int images = R.drawable.image;



    List<ItemsModel> listItems = new ArrayList<>();

    CustomeAdapter customeAdapter;
    Toolbar reportTbar;

    /**
     * on create method load inital value
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        reportTbar = (Toolbar) findViewById(R.id.reportToolbar);
        lstView = (ListView) findViewById(R.id.lstReceipt);
        btnRpt = (Button) findViewById(R.id.btnReport);

        List<Receipt> rec = dbOpener.selectAllReceipt();
        if ( rec != null ){

            for (int j = 0; j < rec.size(); j++) {
                uId = String.valueOf(rec.get(j).getId());
                uDate = (rec.get(j).getDate()).substring(4, 7).trim() + "-" + (rec.get(j).getDate()).substring(8, 10).trim() + "-" + (rec.get(j).getDate()).substring(24, 28).trim() ;
                uSub = String.valueOf(rec.get(j).getTotalPrice());
                uTax = String.valueOf(rec.get(j).getTaxDeductionTotal());
                uItems = rec.get(j).getProducts();

                ItemsModel itemsModel = new ItemsModel(uId, uDate,uItems, uSub, uTax);
                listItems.add(itemsModel);
            }
/**
 * set on click listener for the receipt find
 */
            btnRpt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ReportActivity.this, ReportMActivity.class));

                }
            });

/**
 * set on item click listener for the lst view
 */
            lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    ArrayList<String> lstP = new ArrayList<>();



                    //ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lstP);


                    createNewDiaglog(position);

                }
            });



            customeAdapter = new CustomeAdapter(listItems, this);
            lstView.setAdapter(customeAdapter);

        }





    }

    /**
     * create a new diaglog for display the receipt detail
     * @param position
     */
    public void createNewDiaglog(int position){

        AlertDialog.Builder mybuilder = new AlertDialog.Builder(this);

        String str = "   ID: " + listItems.get(position).getrId() + "    DATE: " + listItems.get(position).getrDate() + "  TOTAL: " +  listItems.get(position).getrSub() + "    TAX(DE.): " + listItems.get(position).getrTax();



        int num = listItems.get(position).getrItem().size() + 1;


        String[] popupStr = new String[num];
        popupStr[0] = " PRODUCT  ID\t\t\t\t\t\tNAME\t\t\t\t\t\t\t\t\tQTY.";


        for (int n = 1; n < num; n++ ){

            popupStr[n] = String.valueOf(listItems.get(position).getrItem().get(n-1).getId()) + "\t\t\t" + (listItems.get(position).getrItem().get(n-1).getProductName()).substring(0,12)  + "\t\t\t\t\t\t" + listItems.get(position).getrItem().get(n-1).getQuantity();
        }


        mybuilder.setTitle("RECEIPT:" + str).setItems(popupStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        myDialog = mybuilder.create();
        myDialog.show();


    }

    /**
     * on create iption menu for the receipt
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        menu.findItem(R.id.reportButton).setVisible(false);

        MenuItem menuItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                customeAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    /**
     * on options item selected by the list for the receipt list
     * @param item
     * @return
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
     * customer adapter for the receipt items show in the list
     */
    public class CustomeAdapter extends BaseAdapter implements Filterable {

        private List<ItemsModel> itemsModelList;
        private List<ItemsModel> itemsModelListFiltered;
        private Context context;

        public CustomeAdapter(List<ItemsModel> itemsModelList, Context context) {
            this.itemsModelList = itemsModelList;
            this.itemsModelListFiltered = itemsModelList;
            this.context = context;
        }

        /**
         * get item number for list
         * @return number of the list item
         */
        @Override
        public int getCount() {
            return itemsModelListFiltered.size();
        }

        /**
         * get item for list
         * @param position
         * @return item of pick
         */

        @Override
        public Object getItem(int position) {
            return null;
        }

        /**
         * get item id for the pick item
         * @param position
         * @return id for the pick item
         */

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * get view for the list view
         * @param position
         * @param convertView
         * @param parent
         * @return a custom view of the list
         */

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row_items,null);

            ImageView imageView = view.findViewById(R.id.itemView);
            TextView itemId = view.findViewById(R.id.itemId);
            TextView itemDate = view.findViewById(R.id.itemDate);
            TextView itemDetail = view.findViewById(R.id.itemDetail);
            TextView itemSub = view.findViewById(R.id.itemSub);
            TextView itemTax = view.findViewById(R.id.itemTax);

            // imageView.setImageResource(itemsModelsFiltered.get(position).getImage());
            itemId.setText("Receipt No.:" + itemsModelListFiltered.get(position).getrId());
            itemDate.setText("Date:" + itemsModelListFiltered.get(position).getrDate());
            itemDetail.setText(""); //+ itemsModelListFiltered.get(position).getrItem());
            itemSub.setText("Total:" + itemsModelListFiltered.get(position).getrSub());
            itemTax.setText("Tax Deduction:" + itemsModelListFiltered.get(position).getrTax());

            return view;
        }

        /**
         * get filter for the receipt find
         * @return key word of the receipt
         */

        @Override
        public Filter getFilter() {

            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults filterResults = new FilterResults();

                    if(constraint == null || constraint.length() == 0){

                        filterResults.count = itemsModelList.size();
                        filterResults.values = itemsModelList;
                    }else{

                        String searhStr = constraint.toString().toLowerCase();
                        List<ItemsModel> resultData = new ArrayList<>();

                        for(ItemsModel itemsModel:itemsModelList){
                            if(itemsModel.getrId().contains(searhStr) || itemsModel.getrDate().contains(searhStr)){
                                resultData.add(itemsModel);
                            }

                            filterResults.count = resultData.size();
                            filterResults.values = resultData;

                        }



                    }

                    return filterResults;
                }

                /**
                 * publich the results to the list view
                 * @param constraint
                 * @param results
                 */
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    itemsModelListFiltered = (List<ItemsModel>) results.values;

                    notifyDataSetChanged();

                }
            };
            return filter;
        }
    }
}