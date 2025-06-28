package com.xdev.communicator.models.production.dto;


import com.xdev.communicator.models.common.dtos.BaseDto;

public class PlanItemDTO  extends BaseDto {
    private String type; // "LOT" or "GLOBAL_LOT"
    private UnifiedDeliveryDTO lot; // Used in getPlanning response

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UnifiedDeliveryDTO getLot() {
        return lot;
    }

    public void setLot(UnifiedDeliveryDTO lot) {
        this.lot = lot;
    }
}