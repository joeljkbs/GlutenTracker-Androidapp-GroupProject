/**
 * Project: cst83334 team 11 project
 * Name: Feng Sun
 * Student id: 040634005
 * Date: 2020-12-05
 */
package com.example.cst8334_glutentracker.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.GlutenDatabase;
import com.example.cst8334_glutentracker.entity.ItemsModel;
import com.example.cst8334_glutentracker.entity.Product;
import com.example.cst8334_glutentracker.entity.Receipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * class ReportMActivity for the monthly and custom report,
 * upload report to google drive and email
 */

public class ReportMActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ListView lstMReport;

    ArrayList<ItemsModel> arrayReceipt;
    StringBuilder data;
    Button btnReport;
    Button btnCsvFile;
    Button btnMonth;
    Button btnSearch;

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    TextView txtA;
    TextView txtFrom;
    TextView txtTo;

    int dateFromD,dateFromM,dateFromY, dateToD,dateToM,dateToY;
    boolean isFrom = false;

    String pickDate,strYear,strMonth,strM,tmpM,strDay,strDate;
    int nDate,tmpDate,startDate,endDate;
    double tmpTotal,tmpTax;

    private GlutenDatabase dbOpener = new GlutenDatabase(this);
    private String uId,uDate,uItem,uSub,uTax;
    private List<Product> uItems;
    ArrayList<String> lstMonthly = new ArrayList<>();
    StringBuilder csvData;

    /**
     * on create method for inital all value for the repot
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_m);

        lstMReport = (ListView) findViewById(R.id.lstMReport);

        btnReport = (Button)findViewById(R.id.btnReceipt);
        btnCsvFile = (Button)findViewById(R.id.btnCsvFile);



        btnMonth = (Button)findViewById(R.id.btnMonth);
        btnSearch = (Button)findViewById(R.id.btnSearch);

        txtFrom =(TextView)findViewById(R.id.txtFrom_data);
        txtTo = (TextView)findViewById(R.id.txtTo_data);

        //arrayList = new ArrayList<>();

        readMonthData();

        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lstMonthly);
        lstMReport.setAdapter(adapter);
/**
 * set on click listener for the report
 */
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
/**
 * set on click listener for the csv file upload
 */
        btnCsvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputCsvFile();
            }
        });
/**
 * set on click listener for the custom report
 */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                readCustomData();
                adapter.notifyDataSetChanged();
                Toast.makeText(ReportMActivity.this,"Report for Monthly",Toast.LENGTH_LONG).show();



            }
        });
/**
 * set on click listener for the monthly report
 */
        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                readMonthData();

                adapter.notifyDataSetChanged();
                Toast.makeText(ReportMActivity.this,"Report for Customer Search",Toast.LENGTH_LONG).show();

            }
        });

        /**
         * set on click listener for the start date of the date picker
         */
        txtFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = true;
                showDatePickerDailog();


            }
        });
/**
 * set on click listener for the end date of the date picker
 */
        txtTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = false;
                showDatePickerDailog();



            }
        });




    }

    /**
     * display a dailog for the date picker
     */
    private void showDatePickerDailog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();

    }

    /**
     * on date set for the picker
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {



        String  m = String.format("%02d", (month+1));
        String d = String.format("%02d", dayOfMonth);

        pickDate = String.valueOf(year) + m + d;

        if( isFrom == false ){

            txtTo.setText(pickDate);
            endDate = Integer.parseInt(pickDate);


        }else{

            txtFrom.setText(pickDate);

            startDate = Integer.parseInt(pickDate);

        }




    }

    /**
     * upload a csv file for the report
     */
    //#34
    public void outputCsvFile()  {
        //generate data
        //StringBuilder cData = new StringBuilder();

        // csvData.append("abc,ddd");

        // saving the file into device
        try {
            FileOutputStream out = openFileOutput("Report.csv", Context.MODE_PRIVATE);
            out.write((csvData.toString().getBytes()));
            out.close();

            //exporting
            Context context = getApplicationContext();
            File fileLocation = new File(getFilesDir(),"Report.csv");
            Uri path = FileProvider.getUriForFile(context,"com.example.cst8334_glutentracker.fileprovider",fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Receipt Report");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM,path);
            startActivity(Intent.createChooser(fileIntent,"Send mail"));


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * read data from database for the monthly report
     */

    private void readMonthData(){
        lstMonthly.clear();
        csvData = new StringBuilder();

        List<Receipt> rec = dbOpener.selectAllReceipt();
        tmpDate = 0;
        tmpTax = 0;
        tmpTotal = 0;
        nDate =0;
        lstMonthly.add("Month" + "\t\t\t\t\t\t\t\t\t" + "Total Amount" + "\t\t\t\t\t\t\t\t\t" + "Total Deduction");
        csvData.append("\n" + "Month" + "," + "Total Amount" + "," + "Total Deduction");


        for (int j = 0; j < rec.size(); j++) {

            strYear = (rec.get(j).getDate()).substring(24, 28).trim();
            strM = (rec.get(j).getDate()).substring(4, 7).trim();
            strDay = (rec.get(j).getDate()).substring(8, 10).trim();


            // nYear = Integer.getInteger(uYear);
            // nDay = Integer.getInteger(uDay);

            switch (strM) {
                case "Jan":
                    strMonth = "1";
                    break;
                case "Feb":
                    strMonth = "2";
                    break;
                case "Mar":
                    strMonth = "3";
                    break;
                case "Apr":
                    strMonth = "4";
                    break;
                case "May":
                    strMonth = "5";
                    break;
                case "Jun":
                    strMonth = "6";
                    break;
                case "Jul":
                    strMonth = "7";
                    break;
                case "Aug":
                    strMonth = "8";
                    break;
                case "Sep":
                    strMonth = "9";
                    break;
                case "Oct":
                    strMonth = "10";
                    break;
                case "Nov":
                    strMonth = "11";
                    break;
                case "Dec":
                    strMonth = "12";
                    break;
            }

            //uSub = rec.get(j).getTotalPrice();
            //uTax = rec.get(j).getTaxDeductionTotal();
            //nDate = Integer.getInteger(uYear + uMonth + uDay);
            strDate = strYear + "-" + strM ;
            nDate = Integer.valueOf((strYear + strMonth).trim());

            if (nDate == tmpDate){

                tmpTotal = tmpTotal + rec.get(j).getTotalPrice();
                tmpTax = tmpTax + rec.get(j).getTaxDeductionTotal();


            }else{
                if (nDate != 0) {
                    if (tmpTotal != 0) {
                        lstMonthly.add(tmpM + "\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTotal + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTax);
                        csvData.append("\n" + tmpM + "," + tmpTotal + "," + tmpTax);
                    }
                    tmpTotal = rec.get(j).getTotalPrice();
                    tmpTax = rec.get(j).getTaxDeductionTotal();

                }
                tmpDate = nDate;
                tmpM = strDate;
            }




        }

        lstMonthly.add(strDate + "\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTotal + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTax );
        csvData.append("\n" +tmpM + "," + tmpTotal + "," + tmpTax);



    }

    /**
     * read data from database for the custom report
     */
    private void readCustomData(){
        lstMonthly.clear();
        csvData = new StringBuilder();

        List<Receipt> rec = dbOpener.selectAllReceipt();
        tmpDate = 0;
        tmpTax = 0;
        tmpTotal = 0;
        lstMonthly.add("Month" + "\t\t\t\t\t\t\t\t\t" + "Total Amount" + "\t\t\t\t\t\t\t\t\t" + "Total Deduction");
        csvData.append( "Month" + "," + "Total Amount" + "," + "Total Deduction");


        for (int j = 0; j < rec.size(); j++) {

            strYear = (rec.get(j).getDate()).substring(24, 28).trim();
            strM = (rec.get(j).getDate()).substring(4, 7).trim();
            strDay = (rec.get(j).getDate()).substring(8, 10).trim();


            // nYear = Integer.getInteger(uYear);
            // nDay = Integer.getInteger(uDay);

            switch (strM) {
                case "Jan":
                    strMonth = "1";
                    break;
                case "Feb":
                    strMonth = "2";
                    break;
                case "Mar":
                    strMonth = "3";
                    break;
                case "Apr":
                    strMonth = "4";
                    break;
                case "May":
                    strMonth = "5";
                    break;
                case "Jun":
                    strMonth = "6";
                    break;
                case "Jul":
                    strMonth = "7";
                    break;
                case "Aug":
                    strMonth = "8";
                    break;
                case "Sep":
                    strMonth = "9";
                    break;
                case "Oct":
                    strMonth = "10";
                    break;
                case "Nov":
                    strMonth = "11";
                    break;
                case "Dec":
                    strMonth = "12";
                    break;
            }

            uSub = String.valueOf(rec.get(j).getTotalPrice());
            uTax = String.valueOf(rec.get(j).getTaxDeductionTotal());

            strDate = strYear + "-" + strM + "-" + strDay;
            nDate = Integer.valueOf((strYear + strMonth + strDay).trim());

            if (nDate >= startDate && nDate <= endDate){

                //tmpTotal = tmpTotal + rec.get(j).getTotalPrice();
                //tmpTax = tmpTax + rec.get(j).getTaxDeductionTotal();
                lstMonthly.add( strDate + "\t\t\t\t\t\t\t\t\t\t\t\t" + uSub + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + uTax);

                csvData.append("\n" + strDate + "," + uSub + "," + uTax);
                tmpTotal = tmpTotal + rec.get(j).getTotalPrice();
                tmpTax =tmpTax + rec.get(j).getTaxDeductionTotal();



            }





        }

        lstMonthly.add("Total:" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTotal + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + tmpTax );
        csvData.append("\n" + "Total:" + "," + tmpTotal + "," + tmpTax);



    }

}