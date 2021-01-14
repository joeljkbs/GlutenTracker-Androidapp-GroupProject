/**
 * Project: cst83334 team 11 project
 * Name: Feng Sun
 * Student id: 040634005
 * Date: 2020-12-05
 */
package com.example.cst8334_glutentracker.entity;

import com.example.cst8334_glutentracker.entity.Product;

import java.util.List;

/**
 * class for the report items model
 */
public class ItemsModel {

    private String rId;
    private String rDate;
    private int rYear;
    private int rMonth;
    private int rDay;
    private  List<Product> rItem;
    private String rSub;
    private String rTax;

    /**
     * the contructor for the receipt find function
     * @param rId   receipt id
     * @param rDate receipt date
     * @param rItem receipt detail
     * @param rSub  receipt total amount
     * @param rTax  receipt tax
     */
    public ItemsModel(String rId, String rDate, List<Product> rItem, String rSub, String rTax) {
        this.rId = rId;
        this.rDate = rDate;
        this.rItem = rItem;
        this.rSub = rSub;
        this.rTax = rTax;
    }

    /**
     * getter for the receipt id
     * @return receipt id
     */

    public String getrId() {
        return rId;
    }

    /**
     * getter for the receipt date
     * @return receipt date
     */

    public String getrDate() {
        return rDate;
    }

    /**
     * getter for the receipt detail
     * @return list of detail of receipt
     */

    public List<Product> getrItem() {
        return rItem;
    }

    /**
     * getter for the total amount of the receipts
     * @return total amount
     */

    public String getrSub() {
        return rSub;
    }

    /**
     * getter for the tax amount
     * @return tax amount
     */

    public String getrTax() {
        return rTax;
    }

    /**
     * setter receipt id
     * @param rId accept receipt id
     */

    public void setrId(String rId) {
        this.rId = rId;
    }

    /**
     * setter receipt date
     * @param rDate accept receipt date
     */

    public void setrDate(String rDate) {
        this.rDate = rDate;
    }

    /**
     * setter receipt item detail
     * @param rItem accept item detail
     */

    public void setrItem(List<Product> rItem) {
        this.rItem = rItem;
    }

    /**
     * setter receipt total amount
     * @param rSub receipt total amount
     */

    public void setrSub(String rSub) {
        this.rSub = rSub;
    }

    /**
     * setter receipt tax
     * @param rTax accept receipt tax
     */

    public void setrTax(String rTax) {
        this.rTax = rTax;
    }

    /**
     * getter receipt year
     * @return year by integer
     */


    public int getrYear() {
        return rYear;
    }

    /**
     * setter year
     * @param rYear accept year by integer
     */

    public void setrYear(int rYear) {
        this.rYear = rYear;
    }

    /**
     * getter month
     * @return accept month by integer
     */

    public int getrMonth() {
        return rMonth;
    }

    /**
     * setter month
     * @param rMonth accept month by integer
     */

    public void setrMonth(int rMonth) {
        this.rMonth = rMonth;
    }

    /**
     * getter for day
     * @return get for day
     */


    public int getrDay() {
        return rDay;
    }

    /**
     * setter for day
     * @param rDay accept day by integer
     */

    public void setrDay(int rDay) {
        this.rDay = rDay;
    }
}