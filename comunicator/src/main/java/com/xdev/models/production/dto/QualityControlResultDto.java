package com.xdev.models.production.dto;




import java.io.Serializable;
import java.util.UUID;



public class QualityControlResultDto implements Serializable {

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