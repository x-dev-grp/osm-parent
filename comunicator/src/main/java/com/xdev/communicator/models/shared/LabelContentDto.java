package com.xdev.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.communicator.models.common.dtos.BaseDto;
import com.xdev.communicator.models.enums.LabelClaimType;
import com.xdev.communicator.models.enums.LabelContentStatus;
import com.xdev.communicator.models.enums.LabelLanguage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelContentDto extends BaseDto {
    private UUID lotId;
    private UUID packagingId;
    private UUID operatorId;
    private LabelContentStatus status;
    private LabelLanguage language;
    private LocalDate packagingDate;
    private String legalDenomination;
    private String originCountry;
    private String netQuantity;
    private String bestBeforeDate;
    private String storageConditions;
    private String responsibleName;
    private String responsibleAddress;
    private String lotNumber;
    private String extractionMethod;
    private String sensoryProfile;
    private List<String> certifications = new ArrayList<>();
    private Set<LabelClaimType> claimTypes = new LinkedHashSet<>();
    private List<String> marketingClaims = new ArrayList<>();
    private String finalPayloadJson;
    private LocalDateTime finalizedAt;
    private String finalizedBy;
    private String publicCode;
    private List<LabelSourceSnapshotDto> sourceSnapshots = new ArrayList<>();
    private List<LabelValidationIssueDto> validationIssues = new ArrayList<>();


    public void setLotId(UUID lotId) {
        this.lotId = lotId;
    }


    public void setPackagingId(UUID packagingId) {
        this.packagingId = packagingId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public LabelContentStatus getStatus() {
        return status;
    }

    public void setStatus(LabelContentStatus status) {
        this.status = status;
    }

    public LabelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }


    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }

    public String getLegalDenomination() {
        return legalDenomination;
    }

    public void setLegalDenomination(String legalDenomination) {
        this.legalDenomination = legalDenomination;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(String netQuantity) {
        this.netQuantity = netQuantity;
    }

    public String getBestBeforeDate() {
        return bestBeforeDate;
    }

    public void setBestBeforeDate(String bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public String getResponsibleName() {
        return responsibleName;
    }

    public void setResponsibleName(String responsibleName) {
        this.responsibleName = responsibleName;
    }

    public String getResponsibleAddress() {
        return responsibleAddress;
    }

    public void setResponsibleAddress(String responsibleAddress) {
        this.responsibleAddress = responsibleAddress;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getSensoryProfile() {
        return sensoryProfile;
    }

    public void setSensoryProfile(String sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public Set<LabelClaimType> getClaimTypes() {
        return claimTypes;
    }

    public void setClaimTypes(Set<LabelClaimType> claimTypes) {
        this.claimTypes = claimTypes;
    }

    public List<String> getMarketingClaims() {
        return marketingClaims;
    }

    public void setMarketingClaims(List<String> marketingClaims) {
        this.marketingClaims = marketingClaims;
    }

    public String getFinalPayloadJson() {
        return finalPayloadJson;
    }

    public void setFinalPayloadJson(String finalPayloadJson) {
        this.finalPayloadJson = finalPayloadJson;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public String getFinalizedBy() {
        return finalizedBy;
    }

    public void setFinalizedBy(String finalizedBy) {
        this.finalizedBy = finalizedBy;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public List<LabelSourceSnapshotDto> getSourceSnapshots() {
        return sourceSnapshots;
    }

    public void setSourceSnapshots(List<LabelSourceSnapshotDto> sourceSnapshots) {
        this.sourceSnapshots = sourceSnapshots;
    }

    public List<LabelValidationIssueDto> getValidationIssues() {
        return validationIssues;
    }

    public void setValidationIssues(List<LabelValidationIssueDto> validationIssues) {
        this.validationIssues = validationIssues;
    }
}
