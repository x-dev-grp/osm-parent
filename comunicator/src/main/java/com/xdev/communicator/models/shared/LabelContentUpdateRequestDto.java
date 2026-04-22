package com.xdev.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.communicator.models.enums.LabelClaimType;
import com.xdev.communicator.models.enums.LabelLanguage;
import jdk.jfr.DataAmount;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelContentUpdateRequestDto implements Serializable {
    private LabelLanguage language;
    private LocalDate packagingDate;
    private String legalDenomination;
    private String storageConditions;
    private String sensoryProfile;
    private List<String> certifications;
    private Set<LabelClaimType> claimTypes;

    public LabelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }

    public LocalDate getPackagingDate() {
        return packagingDate;
    }


    public String getLegalDenomination() {
        return legalDenomination;
    }


    public String getStorageConditions() {
        return storageConditions;
    }


    public String getSensoryProfile() {
        return sensoryProfile;
    }


    public List<String> getCertifications() {
        return certifications;
    }


    public Set<LabelClaimType> getClaimTypes() {
        return claimTypes;
    }

}
