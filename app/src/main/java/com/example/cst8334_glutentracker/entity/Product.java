package com.example.cst8334_glutentracker.entity;

/**
 * This class represents a product instance.
 */
public class Product {

    /**
     * Product's ID.
     */
    private long id = 0;

    /**
     * Product's name.
     */
    private String productName = "default";

    /**
     * Product's description.
     */
    private String productDescription = "default";

    /**
     * Product's price.
     */
    private double price = 0;

    /**
     * Product's display price.
     */
    private double displayedPrice = 0;

    /**
     * Product's quantity.
     */
    private int quantity = 0;

    /**
     * Is product gluten-free?
     */
    private boolean isGlutenFree = false;

    /**
     * Product's linked product.
     */
    private Product linkedProduct = null;


    /**
     * This class's main constructor.
     *
     * @param id product's id.
     * @param productName product's name.
     * @param productDescription product's description.
     * @param price product's price.
     * @param isGlutenFree is product gluten-free?
     */
    public Product(long id, String productName, String productDescription, double price, boolean isGlutenFree){
        setId(id);
        setProductName(productName);
        setProductDescription(productDescription);
        setPrice(price);
        setIsGlutenFree(isGlutenFree);
        setQuantity(1);
        setDisplayedPrice(price);
    }

    /**
     * Setter of product's id.
     * @param id product's id.
     * @return the current instance.
     */
    public Product setId(long id){
        this.id = id;
        return this;
    }

    /**
     * Getter of product's id.
     * @return product's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Setter of product's name.
     * @param productName product's name.
     * @return the current instance.
     */
    public Product setProductName(String productName){
        this.productName = productName;
        return this;
    }

    /**
     * Getter of product's name.
     * @return product's name.
     */
    public String getProductName(){
        return productName;
    }

    /**
     * Setter of product's description.
     * @param productDescription product's description.
     * @return the current instance.
     */
    public Product setProductDescription(String productDescription){
        this.productDescription = productDescription;
        return this;
    }

    /**
     * Getter of product's description.
     * @return product's description.
     */
    public String getProductDescription(){
        return productDescription;
    }

    /**
     * Setter of product's price.
     * @param price product's price.
     * @return the current instance.
     */
    public Product setPrice(double price){
        this.price = price;
        return this;
    }

    /**
     * Getter of product's price.
     * @return product's price.
     */
    public double getPrice(){
        return price;
    }

    /**
     * Setter of product's isGlutenFree.
     * @param isGlutenFree is product gluten-free.
     * @return the current instance.
     */
    public Product setIsGlutenFree(boolean isGlutenFree){
        this.isGlutenFree = isGlutenFree;
        return this;
    }

    /**
     * Getter of product's isGlutenFree.
     * @return weather if the product is gluten-free.
     */
    public boolean isGlutenFree(){
        return isGlutenFree;
    }

    /**
     * Getter of product's displayedPrice.
     * @return product's displayedPrice.
     */
    public double getDisplayedPrice() {
        return displayedPrice;
    }

    /**
     * Setter of product's displayedPrice.
     * @param displayedPrice product's displaye price.
     * @return the current instance.
     */
    public Product setDisplayedPrice(double displayedPrice) {
        this.displayedPrice = displayedPrice;
        return this;
    }

    /**
     * Getter of product's quantity.
     * @return product's quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Setter of product's quantity.
     * @param quantity product's quantity.
     * @return the current instance.
     */
    public Product setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Getter of product's linkedProduct.
     * @return product's linkedProduct.
     */
    public Product getLinkedProduct(){
        return linkedProduct;
    }

    /**
     * Setter of product's linkedProduct.
     * @param linkedProduct product's linked product.
     * @return the current instance.
     */
    public void setLinkedProduct(Product linkedProduct){
        this.linkedProduct = linkedProduct;
    }

    /**
     * Change product's quantity and display price.
     *
     * @param newQuantity product's quantity.
     */
    public void changeQuantityAndDisplayedPrice(int newQuantity){
        setQuantity(newQuantity);
        setDisplayedPrice(getPrice() * getQuantity());
        if(getLinkedProduct() != null){
            getLinkedProduct().setQuantity(getQuantity());
            getLinkedProduct().setDisplayedPrice(getLinkedProduct().getPrice() * getLinkedProduct().getQuantity());
        }
    }

    /**
     * Change product's quantity and price.
     *
     * @param newPrice product's price.
     */
    public void changeQuantityAndOriginalPrice(double newPrice){
        setQuantity(1);
        setPrice(newPrice);
        setDisplayedPrice(getPrice() * getQuantity());
        if (getLinkedProduct() != null) {
            getLinkedProduct().setQuantity(getQuantity());
            getLinkedProduct().setDisplayedPrice(getLinkedProduct().getPrice() * getLinkedProduct().getQuantity());
        }
    }

    /**
     * Getter of product's deduction.
     * @return product's deduction.
     */
    public double getDeduction() {
        if(getLinkedProduct() != null)
            return getDisplayedPrice() - getLinkedProduct().getDisplayedPrice();
        else
            return 0;
    }

    /**
     * Get display price as a String.
     * @return display name as a String.
     */
    public String getDisplayedPriceAsString(){
        return String.format("%.2f", getDisplayedPrice());
    }

    /**
     * Get deduction as a String.
     * @return deduction as a String.
     */
    public String getDeductionAsString(){
        return String.format("%.2f", getDeduction());
    }
}