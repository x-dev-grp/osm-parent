package com.xdev.communicator.models.shared;

import com.xdev.communicator.models.enums.LabelCategory;
import com.xdev.communicator.models.enums.LabelLanguage;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class LabelGenerateRequestDto implements Serializable {
    private UUID lotId;
    private UUID packagingId;
    private UUID operatorId;
    private LocalDate packagingDate;
    private LabelLanguage language = LabelLanguage.FR;
    private LabelCategory labelCategory = LabelCategory.UNIT;

    public LabelCategory getLabelCategory() {
        return labelCategory;
    }
    public UUID getLotId() {
        return lotId;
    }
    public UUID getPackagingId() {
        return packagingId;
    }
    public LocalDate getPackagingDate() {
        return packagingDate;
    }
    public LabelLanguage getLanguage() {
        return language;
    }
    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }
}
