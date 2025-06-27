package com.xdev.models.production.dto;


import java.io.Serializable;

public class PlanItemDTO implements Serializable {
    private String type; // "LOT" or "GLOBAL_LOT"
    private String id; // lotNumber for LOT, globalLotNumber for GLOBAL_LOT
    private UnifiedDeliveryDTO lot; // Used in getPlanning response

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UnifiedDeliveryDTO getLot() {
        return lot;
    }

    public void setLot(UnifiedDeliveryDTO lot) {
        this.lot = lot;
    }
}