package com.xdev.models.production.dto;


import java.util.List;
import java.util.UUID;

public class MillPlanDTO {
    private UUID millMachineId;
    private List<PlanItemDTO> items;

    public UUID getMillMachineId() {
        return millMachineId;
    }

    public void setMillMachineId(UUID millMachineId) {
        this.millMachineId = millMachineId;
    }

    public List<PlanItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PlanItemDTO> items) {
        this.items = items;
    }
}