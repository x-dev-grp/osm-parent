package com.xdev.models.production.dto;


import com.xdev.models.production.enums.TransactionState;
import com.xdev.models.production.enums.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OilTransactionDTO implements Serializable {
    private Boolean isDeleted = false;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private StorageUnitDto storageUnitDestination;
    private StorageUnitDto storageUnitSource;
    private String qualityGrade;
    private Double quantityKg;
    private Double unitPrice;
    private Double totalPrice;
    private TransactionType transactionType;
    private TransactionState transactionState;
    private UnifiedDeliveryDTO reception;

    public UnifiedDeliveryDTO getReception() {
        return reception;
    }

    public void setReception(UnifiedDeliveryDTO reception) {
        this.reception = reception;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }


    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }


    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public StorageUnitDto getStorageUnitDestination() {
        return storageUnitDestination;
    }

    public void setStorageUnitDestination(StorageUnitDto storageUnitDestination) {
        this.storageUnitDestination = storageUnitDestination;
    }

    public StorageUnitDto getStorageUnitSource() {
        return storageUnitSource;
    }

    public void setStorageUnitSource(StorageUnitDto storageUnitSource) {
        this.storageUnitSource = storageUnitSource;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }



    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
