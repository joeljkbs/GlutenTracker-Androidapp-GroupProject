package com.example.cst8334_glutentracker.entity;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a receipt instance.
 */
public class Receipt {

    /**
     * Receipt's ID.
     */
    private long id = 0;

    /**
     * Receipt's product list.
     */
    private List<Product> products = new ArrayList<>();

    /**
     * Receipt's file.
     */
    private String receiptFile = "default";

    /**
     * Receipt's total tax deduction.
     */
    private double taxDeductionTotal = 0;

    /**
     * Receipt's total price.
     */
    private double totalPrice = 0;

    /**
     * Receipt's date.
     */
    private String date = "now";

    /**
     * Receipt's image.
     */
    private Bitmap image;

    /**
     * This class's main constructor.
     *
     * @param id Receipt's ID.
     * @param products Receipt's product.
     * @param receiptFile Receipt's file.
     * @param taxDeductionTotal Receipt's total tax deduction.
     * @param totalPrice Receipt's total price.
     * @param date Receipt's date.
     */
    public Receipt(long id, List<Product> products, String receiptFile, double taxDeductionTotal, double totalPrice, String date){
        setId(id).setProducts(products)
                .setReceiptFile(receiptFile)
                .setTaxDeductionTotal(taxDeductionTotal)
                .setTotalPrice(totalPrice)
                .setDate(date);
    }

    /**
     * This class's constructor with an image parameter.
     *
     * @param id Receipt's ID.
     * @param products Receipt's product.
     * @param receiptFile Receipt's file.
     * @param taxDeductionTotal Receipt's total tax deduction.
     * @param totalPrice Receipt's total price.
     * @param date Receipt's date.
     * @param image Receipt's image.
     */
    public Receipt(long id, List<Product> products, String receiptFile, double taxDeductionTotal, double totalPrice, String date, Bitmap image){
        this(id, products, receiptFile, taxDeductionTotal, totalPrice, date);
        setImage(image);
    }

    /**
     * Getter of receipt's ID.
     * @return receipt's ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Setter of id.
     * @param id receipt's id.
     * @return the current instance.
     */
    public Receipt setId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Getter of receipt's product list.
     * @return receipt's product list.
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * Setter of products.
     * @param products receipt's list of products.
     * @return the current instance.
     */
    public Receipt setProducts(List<Product> products) {
        this.products = products;
        return this;
    }

    /**
     * Getter of receipt's file.
     * @return receipt's file.
     */
    public String getReceiptFile() {
        return receiptFile;
    }

    /**
     * Setter of receiptFile.
     * @param receiptFile receipt's file.
     * @return the current instance.
     */
    public Receipt setReceiptFile(String receiptFile) {
        this.receiptFile = receiptFile;
        return this;
    }

    /**
     * Getter of receipt's total tax deduction.
     * @return receipt's total tax deduction.
     */
    public double getTaxDeductionTotal() {
        return taxDeductionTotal;
    }

    /**
     * Setter of taxDeductionTotal.
     * @param taxDeductionTotal receipt's total tac deduction.
     * @return the current instance.
     */
    public Receipt setTaxDeductionTotal(double taxDeductionTotal) {
        this.taxDeductionTotal = taxDeductionTotal;
        return this;
    }

    /**
     * Getter of receipt's date.
     * @return receipt's date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter of date.
     * @param date receipt's date.
     * @return the current instance.
     */
    public Receipt setDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * Getter of receipt's total price.
     * @return receipt's total price.
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Setter of totalPrice.
     * @param totalPrice receipt's total price.
     * @return the current instance.
     */
    public Receipt setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    /**
     * Getter of receipt's image.
     * @return receipt's image.
     */
    public Bitmap getImage(){return image;}

    /**
     * Setter of image.
     * @param image receipt's image.
     * @return the current instance.
     */
    public Receipt setImage(Bitmap image){
        this.image = image;
        return this;
    }
}
