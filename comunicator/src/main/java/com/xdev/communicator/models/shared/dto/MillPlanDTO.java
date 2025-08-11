package com.xdev.communicator.models.shared.dto;


import java.util.List;
import java.util.UUID;

public class MillPlanDTO {
    private UUID millMachineId;
    private List<PlanItemDTO> items;

    // Constructor for getPlanning
    public MillPlanDTO(UUID millMachineId, List<PlanItemDTO> items) {
        this.millMachineId = millMachineId;
        this.items = items;
    }
}