package com.xdev.communicator.models.common.dtos;


public class SupplierDto  extends  BaseDto {
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