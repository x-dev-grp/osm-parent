package com.xdev.communicator.models.production.dto;




import com.xdev.communicator.models.common.dtos.BaseDto;

import java.util.UUID;



public class QualityControlResultDto  extends BaseDto {

    QualityControlRuleDto rule;
    String measuredValue;
    private UUID deliveryId;



    public QualityControlRuleDto getRule() {
        return rule;
    }

    public void setRule(QualityControlRuleDto rule) {
        this.rule = rule;
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(String measuredValue) {
        this.measuredValue = measuredValue;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
}