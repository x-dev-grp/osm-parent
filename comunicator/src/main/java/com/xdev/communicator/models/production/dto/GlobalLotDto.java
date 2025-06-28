package com.xdev.communicator.models.production.dto;


import com.xdev.communicator.models.common.dtos.BaseDto;

import java.util.List;

 public class GlobalLotDto  extends BaseDto {
    private String globalLotNumber;
    private double totalKg;
    private List<UnifiedDeliveryDTO> lots;

     public String getGlobalLotNumber() {
         return globalLotNumber;
     }

     public void setGlobalLotNumber(String globalLotNumber) {
         this.globalLotNumber = globalLotNumber;
     }

     public double getTotalKg() {
         return totalKg;
     }

     public void setTotalKg(double totalKg) {
         this.totalKg = totalKg;
     }

     public List<UnifiedDeliveryDTO> getLots() {
         return lots;
     }

     public void setLots(List<UnifiedDeliveryDTO> lots) {
         this.lots = lots;
     }
 }