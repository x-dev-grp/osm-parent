package com.xdev.communicator.models.shared.dto;


import com.xdev.communicator.models.common.dtos.BaseTypeDto;

public class SupplierDto extends BaseTypeDto {
    private SupplierInfoDto supplierInfo;
    private BaseTypeDto genericSupplierType;
    private Boolean hasStorage;

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }



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