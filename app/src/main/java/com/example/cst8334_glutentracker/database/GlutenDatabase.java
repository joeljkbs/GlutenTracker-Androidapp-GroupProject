package com.example.cst8334_glutentracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.cst8334_glutentracker.entity.Product;
import com.example.cst8334_glutentracker.entity.Receipt;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a SQLiteDatabase object and helps the application to interact with it through
 * (C-R-U-D) methods.
 */
public class GlutenDatabase extends SQLiteOpenHelper {

    /**
     * Database's name.
     */
    public static final String DATABASE_NAME = "GlutenTracker.db";

    /**
     * Database's version number.
     */
    public static final int DATABASE_VERSION = 2;

    /**
     * SQLite database object.
     */
    private static SQLiteDatabase db = null;

    /**
     * Error tag for debug purpose.
     */
    private static final String ERROR_TAG = "GlutenDatabase";

    /**
     * Main constructor of this class.
     * @param context To use for locating paths to the the database.
     */
    public GlutenDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS);
        db.execSQL(SQL_CREATE_RECEIPTS);
        db.execSQL(SQL_CREATE_PRODUCT_RECEIPT);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PRODUCTS);
        db.execSQL(SQL_DELETE_RECEIPTS);
        db.execSQL(SQL_DELETE_PRODUCT_RECEIPT);
        onCreate(db);
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
     * However, this method is not abstract, so it is not mandatory for a customer to
     * implement it. If not overridden, default implementation will reject downgrade and
     * throws SQLiteException
     *
     * <p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PRODUCTS);
        db.execSQL(SQL_DELETE_RECEIPTS);
        db.execSQL(SQL_DELETE_PRODUCT_RECEIPT);
        onCreate(db);
    }

    /**
     * This method inserts a list of new products into the database.
     *
     * @param products A list of new products.
     * @return A list of products that were not inserted into the database.
     */
    public List<Product> insertIntoProductsTable(List<Product> products){
        List<Product> failedInsertList = new ArrayList<>();
        for(Product product: products){
            if(insertIntoProductsTable(product) == -1) failedInsertList.add(product);
            if(product.getLinkedProduct()!= null)//added by Joel
                if(insertIntoProductsTable(product.getLinkedProduct()) == -1) failedInsertList.add(product);//added by Joel

        }

        return failedInsertList;
    }

    /**
     * This method inserts a new product into the database.
     *
     * @param product The new product that need to be added into the database.
     * @return The id of the product in the database. If insertion fails, return -1 instead.
     */
    public long insertIntoProductsTable(Product product){
        db = getWritableDatabase();
        if(selectProductByID(product.getId()) != null){
            Log.e(ERROR_TAG, "This product is already available in the database");
            return -1;
        }

        ContentValues cv = new ContentValues();
        //Added by Joel
        cv.put(Products.COLUMN_NAME_ID, product.getId());
        cv.put(Products.COLUMN_NAME_PRODUCT_NAME, product.getProductName());
        cv.put(Products.COLUMN_NAME_DESCRIPTION, product.getProductDescription());
        cv.put(Products.COLUMN_NAME_PRICE, product.getPrice());
        cv.put(Products.COLUMN_NAME_GLUTEN, (product.isGlutenFree() ? 1:0));
        return db.insert(Products.TABLE_NAME, null, cv);
    }

    /**
     * This method inserts a new receipt into the database.
     *
     * @param receipt The new receipt that need to be added into the database.
     * @return The id of the receipt in the database. If insertion fails, return -1 instead.
     */
    public long insertIntoReceiptsTable(Receipt receipt){
        return insertIntoReceiptsTable(receipt.getProducts(),
                receipt.getReceiptFile(),
                receipt.getTaxDeductionTotal(),
                receipt.getTotalPrice(),
                receipt.getDate());
    }

    /**
     * This method inserts a new receipt into the database.
     *
     * @param products A list of products of the receipt.
     * @param file
     * @param totalDeduction The receipt's total deduction.
     * @param totalPrice The receipt's total price.
     * @param date The insert date of the receipt.
     * @return The id of the receipt in the database. If insertion fails, return -1 instead.
     */
    public long insertIntoReceiptsTable(List<Product> products, String file, double totalDeduction, double totalPrice, String date){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(Receipts.COLUMN_NAME_FILE, file);
        cv.put(Receipts.COLUMN_NAME_TOTAL_DEDUCTION, totalDeduction);
        cv.put(Receipts.COLUMN_NAME_TOTAL_PRICE, totalPrice);
        cv.put(Receipts.COLUMN_NAME_DATE, date);
        long id = db.insert(Receipts.TABLE_NAME, null, cv);

        if(!insertIntoProductReceiptTable(products, id)) return -1;
        return id;
    }

    /**
     * This method inserts a new receipt into the database with an image.
     *
     * @param products A list of products of the receipt.
     * @param file
     * @param totalDeduction The receipt's total deduction.
     * @param totalPrice The receipt's total price.
     * @param date The insert date of the receipt.
     * @param image The image of the receipt
     * @return The id of the receipt in the database. If insertion fails, return -1 instead.
     */
    public long insertIntoReceiptsTableWithImage(List<Product> products, String file, double totalDeduction, double totalPrice, String date, Bitmap image){
        db = getWritableDatabase();
        // Learned how to convert Bitmap image to BLOB from https://stackoverflow.com/questions/11790104/how-to-storebitmap-image-and-retrieve-image-from-sqlite-database-in-android
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        byte[] imageToBytes = byteArrayOutputStream.toByteArray();

        ContentValues cv = new ContentValues();

        cv.put(Receipts.COLUMN_NAME_FILE, file);
        cv.put(Receipts.COLUMN_NAME_TOTAL_DEDUCTION, totalDeduction);
        cv.put(Receipts.COLUMN_NAME_TOTAL_PRICE, totalPrice);
        cv.put(Receipts.COLUMN_NAME_DATE, date);
        cv.put(Receipts.COLUMN_NAME_IMAGE, imageToBytes);
        long id = db.insert(Receipts.TABLE_NAME, null, cv);

        if(!insertIntoProductReceiptTable(products, id)) return -1;
        return id;
    }

    /**
     * This method inserts a list of products of a receipt into the database.
     *
     * @param products List of products of the receipt.
     * @param receiptID the ID of the receipt.
     * @return Return true if all insertions  success, otherwise return false.
     */
    private boolean insertIntoProductReceiptTable(List<Product> products, long receiptID){
        db = getWritableDatabase();
        ContentValues cv;

        for(Product product: products) {
            cv = new ContentValues();
            Product linkedProduct = null;
            linkedProduct=product.getLinkedProduct();
            if (linkedProduct != null) {
                cv.put(ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_ID, linkedProduct.getId());
                cv.put(ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_PRICE, linkedProduct.getPrice());
                cv.put(ProductReceipt.COLUMN_NAME_DEDUCTION, product.getDeduction());
            }

            cv.put(ProductReceipt.COLUMN_NAME_PRODUCT_ID, product.getId());
            cv.put(ProductReceipt.COLUMN_NAME_RECEIPT_ID, receiptID);
            cv.put(ProductReceipt.COLUMN_NAME_PRICE, product.getPrice());
            cv.put(ProductReceipt.COLUMN_NAME_QUANTITY, product.getQuantity());
            if(db.insert(ProductReceipt.TABLE_NAME, null, cv) == -1) return false;
        }
        return true;
    }

    /**
     * This method uses an id to get a product from the database.
     *
     * @param id Id of the product.
     * @return A Product object that has the data from the database. Return null if the product
     * can't be found or an error occurs.
     */
    public Product selectProductByID(long id){
        db = getWritableDatabase();
        Cursor cs;
        Product product;
        try{
            cs = db.query(false, Products.TABLE_NAME, null,
                    Products.COLUMN_NAME_ID + " = ? ", new String[]{Long.toString(id)},
                    null, null, null, null, null);
        cs.moveToFirst();

        product = new Product(cs.getLong(0),
                cs.getString(1),
                cs.getString(2),
                cs.getDouble(3),
                cs.getInt(4) == 1); // changed from 0 to 1
    }catch(Exception e){
        Log.e(ERROR_TAG, "Unable to select product", e);
        return null;
    }
        cs.close();
        return product;
    }

    /**
     * This method selects all products that are gluten-free or non gluten-free depends on its
     * parameter.
     *
     * @param isGlutenFree The decision if this method returns gluten-free products or non
     *                     gluten-free products.
     * @return A list of products that are gluten-free or non gluten-free.
     */
    private List<Product> selectProductsByIsGlutenFree(boolean isGlutenFree){
        db = getWritableDatabase();
        List<Product> results = new ArrayList<>();
        Cursor cs;
        try{
            cs = db.query(false, Products.TABLE_NAME, null,
                    null, null, null, null, null,
                    null, null);
            cs.moveToFirst();

            do {
                if((cs.getInt(4) == 1) == isGlutenFree) {
                    Product product = new Product(cs.getLong(0),
                            cs.getString(1),
                            cs.getString(2),
                            cs.getDouble(3),
                            cs.getInt(4) == 1);
                    results.add(product);
                }
            }while (cs.moveToNext());
        }catch(Exception e){
            Log.e(ERROR_TAG, "Unable to select products", e);
            return null;
        }
        cs.close();
        return results;
    }

    /**
     * This method select all gluten-free products from the database.
     *
     * @return A list of gluten-free products, return null if no product was found or an error
     * occurs
     */
    public List<Product> selectProductsByGlutenFree(){
        return selectProductsByIsGlutenFree(true);
    }

    /**
     * This method select all gluten-free products from the database.
     *
     * @return A list of non gluten-free products, return null if no product was found or an error
     * occurs
     */
    public List<Product> selectProductsByNonGlutenFree(){
        return selectProductsByIsGlutenFree(false);
    }

    /**
     * This method uses an id to get a receipt from the database.
     *
     * @param id Id of the receipt.
     * @return A Receipt object that has the data from the database. Return null if the receipt
     * can't be found or an error occurs.
     */
    public Receipt selectReceiptByID(long id){
        db = getWritableDatabase();
        Cursor cs;
        Receipt receipt;
        try {
            cs = db.query(false, Receipts.TABLE_NAME, null,
                    Receipts.COLUMN_NAME_ID + " = ? ", new String[]{Long.toString(id)},
                    null, null, null, null, null);
            cs.moveToNext();
            receipt = new Receipt(cs.getLong(0),
                    selectProductReceipt(id),
                    cs.getString(1),
                    cs.getDouble(3),
                    cs.getDouble(2),
                    cs.getString(4));
        }catch (Exception e){
            Log.e(ERROR_TAG, "Unable to select receipt", e);
            return null;
        }
        cs.close();
        return receipt;
    }

    /**
     * This method uses an id to get a receipt with an image from the database.
     *
     * @param id Id of the receipt.
     * @return A Receipt object that has the data from the database. Return null if the receipt
     * can't be found or an error occurs.
     */
    public Receipt selectReceiptByIDWithImage(long id){
        db = getWritableDatabase();
        Cursor cs;
        Receipt receipt;
        try {
            cs = db.query(false, Receipts.TABLE_NAME, null,
                    Receipts.COLUMN_NAME_ID + " = ? ", new String[]{Long.toString(id)},
                    null, null, null, null, null);
            cs.moveToNext();
            // Learned about how to decode the BLOB from https://stackoverflow.com/questions/11790104/how-to-storebitmap-image-and-retrieve-image-from-sqlite-database-in-android
            byte[] imageAsByte = cs.getBlob(5);
            Bitmap image = BitmapFactory.decodeByteArray(imageAsByte, 0, imageAsByte.length);
            receipt = new Receipt(cs.getLong(0),
                    selectProductReceipt(id),
                    cs.getString(1),
                    cs.getDouble(3),
                    cs.getDouble(2),
                    cs.getString(4),
                    image);
        }catch (Exception e){
            Log.e(ERROR_TAG, "Unable to select receipt", e);
            return null;
        }
        cs.close();
        return receipt;
    }

    /**
     * Select all the receipts in the database.
     *
     * @return A list of all receipts.
     */
    public List<Receipt> selectAllReceipt(){
        db = getWritableDatabase();
        Cursor cs;
        List<Receipt > receipts = new ArrayList<>();
        try {
            cs = db.query(false, Receipts.TABLE_NAME, null,
                    null,null, null, null, null,
                    null, null);
            cs.moveToFirst();
            do{
                receipts.add(new Receipt(cs.getLong(0),
                        selectProductReceipt(cs.getLong(0)),
                        cs.getString(1),
                        cs.getDouble(3),
                        cs.getDouble(2),
                        cs.getString(4)));
            }while(cs.moveToNext());
        }catch (Exception e){
            Log.e(ERROR_TAG, "Unable to select receipts", e);
            return null;
        }
        cs.close();
        return receipts;
    }

    /**
     * This method gets all the receipts from the database with their images
     * @return All the receipts found in the database
     */
    public List<Receipt> selectAllReceiptWithImage(){
        db = getWritableDatabase();
        Cursor cs;
        List<Receipt > receipts = new ArrayList<>();
        try {
            cs = db.query(false, Receipts.TABLE_NAME, null,
                    null,null, null, null, null,
                    null, null);
            cs.moveToFirst();
            do{
                // Learned about how to decode the BLOB from https://stackoverflow.com/questions/11790104/how-to-storebitmap-image-and-retrieve-image-from-sqlite-database-in-android
                byte[] imageAsByte = cs.getBlob(5);
                Bitmap image = BitmapFactory.decodeByteArray(imageAsByte, 0, imageAsByte.length);
                receipts.add(new Receipt(cs.getLong(0),
                        selectProductReceipt(cs.getLong(0)),
                        cs.getString(1),
                        cs.getDouble(3),
                        cs.getDouble(2),
                        cs.getString(4), image));
            }while(cs.moveToNext());
        }catch (Exception e){
            Log.e(ERROR_TAG, "Unable to select receipts", e);
            return null;
        }
        cs.close();
        return receipts;
    }

    /**
     * This method uses a receipt's ID to get that receipt's list of products from the database.
     *
     * @param receiptID Id of the receipt.
     * @return The receipt's list of product from the database in a List object. Return null if data
     * can't be found or an error occurs.
     */
    private List<Product> selectProductReceipt(long receiptID){
        db = getWritableDatabase();
        Cursor cs;
        List<Product> products = new ArrayList<>();
        try {
            cs = db.query(false, ProductReceipt.TABLE_NAME, null,
                    ProductReceipt.COLUMN_NAME_RECEIPT_ID + " = ? ", new String[]{Long.toString(receiptID)},
                    null, null, null, null, null);
            cs.moveToFirst();

            do{
                Product newProduct = selectProductByID(cs.getLong(0));
                if(newProduct == null){
                    Log.e(ERROR_TAG, "Unable to find an item");
                    return null;
                }
                newProduct.setPrice(cs.getDouble(2));
                newProduct.setQuantity(cs.getInt(3));
                //Added by Joel
                newProduct.setDisplayedPrice(newProduct.getPrice()*newProduct.getQuantity());

                Product linkedProduct = selectProductByID(cs.getLong(5));
                if(linkedProduct != null){
                    linkedProduct.setPrice(cs.getDouble(6));
                    linkedProduct.setQuantity(cs.getInt(3));
                    newProduct.setLinkedProduct(linkedProduct);
                }
                products.add(newProduct);
            }while(cs.moveToNext());
        }catch (Exception e){
            Log.e(ERROR_TAG, "Unable to select from productReceipt", e);
            return null;
        }
        cs.close();
        return products;
    }

    /**
     * This method uses a receipt's id to delete it from the database.
     *
     * @param id The receipt's id.
     * @return true if the receipt is deleted successfully, otherwise return false.
     */
    public boolean deleteReceiptByID(long id){
        db = getWritableDatabase();
        db.delete(ProductReceipt.TABLE_NAME,
                ProductReceipt.COLUMN_NAME_RECEIPT_ID + " = ? ",
                new String[]{Long.toString(id)});
        return db.delete(Receipts.TABLE_NAME,
                Receipts.COLUMN_NAME_ID + " = ?",
                new String[]{Long.toString(id)}) != 0;
    }

    /**
     * This method uses a product's id to delete it from the database.
     *
     * @param id The product's id.
     * @return true if the product is deleted successfully, otherwise return false.
     */
    public boolean deleteProductByID(SQLiteDatabase db, long id){
        db = getWritableDatabase();
        return db.delete(Products.TABLE_NAME,
                Products.COLUMN_NAME_ID + " = ? ",
                new String[]{Long.toString(id)}) != 0;
    }

    /**
     * This method updates a product in the database.
     *
     * @param product The product that needs to be updated.
     * @return true if the product is updated successfully, otherwise return false.
     */
    public boolean updateProductById(Product product){
        db = getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(Products.COLUMN_NAME_ID, product.getId());
        cv.put(Products.COLUMN_NAME_PRODUCT_NAME, product.getProductName());
        cv.put(Products.COLUMN_NAME_DESCRIPTION, product.getProductDescription());
        cv.put(Products.COLUMN_NAME_PRICE, product.getPrice());
        cv.put(Products.COLUMN_NAME_GLUTEN, product.isGlutenFree());
        return db.update(Products.TABLE_NAME,cv,
                Products.COLUMN_NAME_ID+" = ? ",
                new String[]{Long.toString(product.getId())}) != 0;
    }

    /**
     * This method updates a receipt by its id
     * @param receipt The receipt to be updated
     * @return True if the receipt is updated, false otherwise
     */
    public boolean updateReceiptById(Receipt receipt){
        db = getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(Receipts.COLUMN_NAME_FILE, receipt.getReceiptFile());
        cv.put(Receipts.COLUMN_NAME_TOTAL_DEDUCTION, receipt.getTaxDeductionTotal());
        cv.put(Receipts.COLUMN_NAME_TOTAL_PRICE, receipt.getTotalPrice());
        cv.put(Receipts.COLUMN_NAME_DATE, receipt.getDate());
        return db.update(Receipts.TABLE_NAME,cv,
                Receipts.COLUMN_NAME_ID+" = ? ",
                new String[]{Long.toString(receipt.getId())}) != 0;
    }

    /**
     * This method updates the ProductReceipt table by the ProductID and ReceiptID
     * @param product The product to be updated
     * @param index The receiptId
     * @return True if the row was updated, false otherwise
     */
    public boolean updateProductReceiptById(Product product, long index){
        db = getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(ProductReceipt.COLUMN_NAME_PRODUCT_ID, product.getId());
        cv.put(ProductReceipt.COLUMN_NAME_RECEIPT_ID, index);
        cv.put(ProductReceipt.COLUMN_NAME_PRICE, product.getPrice());
        cv.put(ProductReceipt.COLUMN_NAME_QUANTITY, product.getQuantity());
        if(product.getLinkedProduct() != null){
            cv.put(ProductReceipt.COLUMN_NAME_DEDUCTION, product.getDeduction());
            cv.put(ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_ID, product.getLinkedProduct().getId());
            cv.put(ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_PRICE, product.getLinkedProduct().getPrice());
        }

        return db.update(ProductReceipt.TABLE_NAME,cv,
                ProductReceipt.COLUMN_NAME_RECEIPT_ID+" = ? AND "+ProductReceipt.COLUMN_NAME_PRODUCT_ID+" = ? ",
                new String[]{Long.toString(index),Long.toString(product.getId())}) != 0;
    }

    /**
     * The query to create the products table.
     */
    private static final String SQL_CREATE_PRODUCTS = "CREATE TABLE " +
            Products.TABLE_NAME + " (" +
            Products.COLUMN_NAME_ID + " BIGINT PRIMARY KEY, " +
            Products.COLUMN_NAME_PRODUCT_NAME + " TEXT, " +
            Products.COLUMN_NAME_DESCRIPTION + " TEXT, " +
            Products.COLUMN_NAME_PRICE + " REAL, " +
            Products.COLUMN_NAME_GLUTEN + " INTEGER)";

    /**
     * The query to create the receipts table.
     */
    private static final String SQL_CREATE_RECEIPTS = "CREATE TABLE " +
            Receipts.TABLE_NAME + " (" +
            Receipts.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Receipts.COLUMN_NAME_FILE + " TEXT, " +
            Receipts.COLUMN_NAME_TOTAL_PRICE + " REAL, " +
            Receipts.COLUMN_NAME_TOTAL_DEDUCTION + " REAL, " +
            Receipts.COLUMN_NAME_DATE + " TEXT, " +
            Receipts.COLUMN_NAME_IMAGE + " BLOB)";

    /**
     * The query to create the productReceipt table.
     */
    private static final String SQL_CREATE_PRODUCT_RECEIPT = "CREATE TABLE " +
            ProductReceipt.TABLE_NAME + " (" +
            ProductReceipt.COLUMN_NAME_PRODUCT_ID + " BIGINT, " +
            ProductReceipt.COLUMN_NAME_RECEIPT_ID + " INTEGER, " +
            ProductReceipt.COLUMN_NAME_PRICE + " REAL, " +
            ProductReceipt.COLUMN_NAME_QUANTITY + " INTEGER, " +
            ProductReceipt.COLUMN_NAME_DEDUCTION + " REAL, " +
            ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_ID + " BIGINT, " +
            ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_PRICE + " REAL, " +
            "CONSTRAINT " + "fk_" + ProductReceipt.TABLE_NAME +
            Products.TABLE_NAME + " FOREIGN KEY (" +
            ProductReceipt.COLUMN_NAME_PRODUCT_ID + ") REFERENCES " +
            Products.TABLE_NAME + "(" +
            Products.COLUMN_NAME_ID + "), " +
            "CONSTRAINT " + "fk_" + ProductReceipt.TABLE_NAME + "linked" +
            Products.TABLE_NAME + " FOREIGN KEY (" +
            ProductReceipt.COLUMN_NAME_LINKED_PRODUCT_ID + ") REFERENCES " +
            Products.TABLE_NAME + "(" +
            Products.COLUMN_NAME_ID + "), " +
            "CONSTRAINT " + "fk_" + ProductReceipt.TABLE_NAME +
            Receipts.TABLE_NAME + " FOREIGN KEY (" +
            ProductReceipt.COLUMN_NAME_RECEIPT_ID + ") REFERENCES " +
            Receipts.TABLE_NAME + "(" +
            Receipts.COLUMN_NAME_ID + "))";

    /**
     * The query to delete the products table.
     */
    private static final String SQL_DELETE_PRODUCTS = "DROP TABLE IF EXISTS " +
            Products.TABLE_NAME;

    /**
     * The query to delete the receipts table.
     */
    private static final String SQL_DELETE_RECEIPTS = "DROP TABLE IF EXISTS " +
            Receipts.TABLE_NAME;

    /**
     * The query to delete the productReceipt table.
     */
    private static final String SQL_DELETE_PRODUCT_RECEIPT = "DROP TABLE IF EXISTS " +
            ProductReceipt.TABLE_NAME;

    /**
     * This class is used to hold the names of the table and columns for the Products table
     */
    private static class Products {
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_NAME_ID = "productID";
        public static final String COLUMN_NAME_PRODUCT_NAME = "productName";
        public static final String COLUMN_NAME_DESCRIPTION = "productDescription";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_GLUTEN = "isGlutenFree";
    }

    /**
     * This class is used to hold the names of the table and columns for the Receipts table
     */
    private static class Receipts {
        public static final String TABLE_NAME = "receipts";
        public static final String COLUMN_NAME_ID = "receiptID";
        public static final String COLUMN_NAME_FILE = "receiptFile";
        public static final String COLUMN_NAME_TOTAL_DEDUCTION = "totalTaxDeduction";
        public static final String COLUMN_NAME_TOTAL_PRICE = "totalPrice";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_IMAGE = "image"; // added by Naimul
    }

    /**
     * This class is used to hold the names of the table and columns for the ProductReceipt table
     */
    private static class ProductReceipt{
        public static final String TABLE_NAME = "productReceipt";
        public static final String COLUMN_NAME_PRODUCT_ID = "productID";
        public static final String COLUMN_NAME_RECEIPT_ID = "receiptID";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_DEDUCTION = "deduction";
        public static final String COLUMN_NAME_LINKED_PRODUCT_ID = "linkedProductID";
        public static final String COLUMN_NAME_LINKED_PRODUCT_PRICE = "linkedProductPrice";

    }
}
