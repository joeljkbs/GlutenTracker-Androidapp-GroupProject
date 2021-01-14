package com.example.cst8334_glutentracker.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cst8334_glutentracker.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the activity where the user can navigate to other activities or sign out.
 */
public class MainMenuActivity extends AppCompatActivity {

    /**
     * Request code from this activity;
     */
    public static final int REQUEST_CODE = 0;

    /**
     * Result code from scanner activity;
     */
    public static final int RESULT_CODE_NAVIGATE_TO_SCANNER = 1;

    /**
     * Result code from cart activity;
     */
    public static final int RESULT_CODE_NAVIGATE_TO_CART = 2;

    /**
     * Result code from receipt activity;
     */
    public static final int RESULT_CODE_NAVIGATE_TO_RECEIPT = 3;

    /**
     * Result code from report activity;
     */
    public static final int RESULT_CODE_NAVIGATE_TO_REPORT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ListView mainMenu = findViewById(R.id.main_menu);
        List<MenuItem> menu = new ArrayList<>();
        MenuAdapter adapter = new MenuAdapter(menu, this);
        mainMenu.setAdapter(adapter);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.navigation_icon);

        menu.add(new MenuItem(R.drawable.barcode_icon,
                RESULT_CODE_NAVIGATE_TO_SCANNER,
                getString(R.string.scanner_activity)));
        menu.add(new MenuItem(R.drawable.cart_icon,
                RESULT_CODE_NAVIGATE_TO_CART,
                getString(R.string.cart_activity)));
        menu.add(new MenuItem(R.drawable.receipt_icon,
                RESULT_CODE_NAVIGATE_TO_RECEIPT,
                getString(R.string.receipt_activity)));
        menu.add(new MenuItem(R.drawable.report_icon,
                RESULT_CODE_NAVIGATE_TO_REPORT,
                getString(R.string.report_activity)));
        adapter.notifyDataSetChanged();

        mainMenu.setOnItemClickListener((AdapterView<?> list, View view, int position, long id) -> {
            navigateToActivity(menu.get(position).getButtonNavigateCode());
        });

        navigationView.setNavigationItemSelectedListener(i -> {
            navigateToActivity(i.getOrder());
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        navigateToActivity(resultCode);
    }

    /**
     * This method will navigate the current activity to another activity.
     *
     * @param buttonNavigateCode code of the target activity.
     */
    private void navigateToActivity(int buttonNavigateCode){
        switch (buttonNavigateCode){
            case RESULT_CODE_NAVIGATE_TO_SCANNER: {
                startActivityForResult(
                        new Intent(MainMenuActivity.this, ScanActivity.class),
                        REQUEST_CODE);
                break;
            }

            case RESULT_CODE_NAVIGATE_TO_CART: {
                startActivityForResult(
                        new Intent(MainMenuActivity.this, CartActivity.class),
                        REQUEST_CODE);
                break;
            }

            case RESULT_CODE_NAVIGATE_TO_RECEIPT: {
                startActivityForResult(
                        new Intent(MainMenuActivity.this, ReceiptActivity.class),
                        REQUEST_CODE);
                break;
            }

            case RESULT_CODE_NAVIGATE_TO_REPORT: {
                startActivityForResult(
                        new Intent(MainMenuActivity.this, ReportActivity.class),
                        REQUEST_CODE);
                break;
            }

            default: break;
        }
    }

    /**
     * This class represents an item of the main menu.
     */
    private class MenuItem {
        /**
         * Resource ID of the item's icon.
         */
        int iconId;

        /**
         * Navigate code of the item.
         */
        int buttonNavigateCode;

        /**
         * Name of the item.
         */
        String buttonName;

        /**
         * Constructor of this class.
         *
         * @param iconId item's icon ID.
         * @param buttonNavigateCode item's navigate code.
         * @param buttonName item's name.
         */
        MenuItem(int iconId, int buttonNavigateCode, String buttonName){
            setIconId(iconId)
                    .setButtonNavigateCode(buttonNavigateCode)
                    .setButtonName(buttonName);
        }

        /**
         * Setter of icon's ID.
         *
         * @param iconId icon's ID.
         * @return the current instance.
         */
        MenuItem setIconId(int iconId){
            this.iconId = iconId;
            return this;
        }

        /**
         * getter of icon's ID.
         *
         * @return icon's ID.
         */
        int getIconId(){
            return iconId;
        }

        /**
         * Setter of navigate code.
         *
         * @param buttonNavigateCode navigate code.
         * @return the current instance.
         */
        MenuItem setButtonNavigateCode(int buttonNavigateCode){
            this.buttonNavigateCode = buttonNavigateCode;
            return this;
        }

        /**
         * getter of navigate code.
         *
         * @return navigate code.
         */
        int getButtonNavigateCode(){
            return buttonNavigateCode;
        }

        /**
         * Setter of item's name.
         *
         * @param buttonName item's name.
         * @return the current instance.
         */
        MenuItem setButtonName(String buttonName){
            this.buttonName = buttonName;
            return this;
        }

        /**
         * getter of item's name.
         *
         * @return item's name.
         */
        String getButtonName(){
            return buttonName;
        }

    }

    /**
     * Adapter of MenuItem.
     */
    private class MenuAdapter extends ArrayAdapter<MenuItem> {

        MenuAdapter(List<MenuItem> menu, Context context){
            super(context, R.layout.menu_item, menu);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.menu_item, parent, false);

            ImageView icon = convertView.findViewById(R.id.icon);
            TextView buttonName = convertView.findViewById(R.id.button_name);

            icon.setImageDrawable(getDrawable(getItem(position).getIconId()));
            buttonName.setText(getItem(position).getButtonName());

            return convertView;
        }
    }

}
