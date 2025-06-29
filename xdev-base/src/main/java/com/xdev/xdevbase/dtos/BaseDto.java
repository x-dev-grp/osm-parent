package com.xdev.xdevbase.dtos;

import com.xdev.xdevbase.entities.BaseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BaseDto<E extends BaseEntity> implements Serializable {
    private UUID id;
    private Set<String> actions = Collections.emptySet();
    private Boolean isDeleted = false;
    private UUID externalId;

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
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

    public UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }
}
