package com.xdev.communicator.models.production.dto;


import com.xdev.communicator.models.common.dtos.BaseDto;

public class DeliveryWithNextIdDto  extends BaseDto {
    private final UnifiedDeliveryDTO delivery;
    private final Long nextId;

    public DeliveryWithNextIdDto(UnifiedDeliveryDTO delivery, Long nextId) {
        this.delivery = delivery;
        this.nextId = nextId;
    }

    public UnifiedDeliveryDTO getDelivery() {
        return delivery;
    }

    public Long getNextId() {
        return nextId;
    }
}
