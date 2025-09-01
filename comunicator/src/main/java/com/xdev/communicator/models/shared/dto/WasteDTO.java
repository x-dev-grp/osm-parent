package com.xdev.communicator.models.shared.dto;


import com.xdev.communicator.models.common.dtos.BaseDto;
import com.xdev.communicator.models.shared.enums.Currency;
import com.xdev.communicator.models.shared.enums.PaymentMethod;
import com.xdev.communicator.models.shared.enums.WasteType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Shared DTO for Waste operations across microservices
 * Used for communication between Production and Finance services
 */
public class WasteDTO extends BaseDto {
    private WasteType type;
    private Double quantityInKg;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Instant saleDate;
    private String invoiceNumber;
    private Boolean paid;
    private Instant paymentDate;
    private String storageLocationCode;
    private UUID customer;
    private SupplierDto supplier;
    private String notes;

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Currency getCurrency() {
        return Currency;
    }

    public void setCurrency(Currency currency) {
        Currency = currency;
    }

    private Double paidAmount;
    private Double unpaidAmount;
    private PaymentMethod paymentMethod;
    private Currency Currency;

    // Getters and Setters

    public WasteType getType() {
        return type;
    }

    public void setType(WasteType type) {
        this.type = type;
    }

    public Double getQuantityInKg() {
        return quantityInKg;
    }

    public void setQuantityInKg(Double quantityInKg) {
        this.quantityInKg = quantityInKg;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Instant getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Instant saleDate) {
        this.saleDate = saleDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStorageLocationCode() {
        return storageLocationCode;
    }

    public void setStorageLocationCode(String storageLocationCode) {
        this.storageLocationCode = storageLocationCode;
    }

    public UUID getCustomer() {
        return customer;
    }

    public void setCustomer(UUID customer) {
        this.customer = customer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


}
