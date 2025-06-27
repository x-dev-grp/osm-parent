package com.xdev.models.finance.dto;


import java.io.Serializable;
import java.time.LocalDate;

public class ExpenseDto implements Serializable {


    private String invoiceRef;
    private String purchaseNature;
    private String object;
    private LocalDate date;
    private Double amount;

    public String getInvoiceRef() {
        return invoiceRef;
    }

    public void setInvoiceRef(String invoiceRef) {
        this.invoiceRef = invoiceRef;
    }

    public String getPurchaseNature() {
        return purchaseNature;
    }

    public void setPurchaseNature(String purchaseNature) {
        this.purchaseNature = purchaseNature;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}