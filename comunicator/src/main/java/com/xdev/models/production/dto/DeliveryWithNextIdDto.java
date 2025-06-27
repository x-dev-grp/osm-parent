package com.xdev.models.production.dto;


import java.io.Serializable;

public class DeliveryWithNextIdDto implements Serializable {
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
