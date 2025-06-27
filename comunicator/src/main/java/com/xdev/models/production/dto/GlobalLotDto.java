package com.xdev.models.production.dto;


import java.util.List;

 public class GlobalLotDto {
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