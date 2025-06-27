package com.xdev.xdevbase.dtos;

import com.xdev.xdevbase.entities.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
public class BaseDto<E extends BaseEntity> implements Serializable {
   private UUID id ;
   private Set<String> actions= Collections.emptySet();
   private Boolean isDeleted = false;

   private String createdBy;

   private LocalDateTime createdDate;

   private String lastModifiedBy;
   private LocalDateTime lastModifiedDate;

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
