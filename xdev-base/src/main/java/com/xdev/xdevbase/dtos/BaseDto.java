package com.xdev.xdevbase.dtos;

import com.xdev.xdevbase.entities.BaseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
public class BaseDto<E extends BaseEntity> implements Serializable {
    UUID id ;
    Set<String> actions= Collections.emptySet();

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
