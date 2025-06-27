package com.xdev.models.production.dto;


import java.io.Serializable;

public class SupplierDto implements Serializable {
    private SupplierInfoDto supplierInfo;
    private BaseTypeDto genericSupplierType;


    public SupplierInfoDto getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(SupplierInfoDto supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public BaseTypeDto getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseTypeDto genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }
}