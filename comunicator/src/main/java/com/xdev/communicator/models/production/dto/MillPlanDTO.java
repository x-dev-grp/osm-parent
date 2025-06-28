package com.xdev.communicator.models.production.dto;


import com.xdev.communicator.models.common.dtos.BaseDto;

import java.util.List;
import java.util.UUID;

public class MillPlanDTO  extends BaseDto {
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