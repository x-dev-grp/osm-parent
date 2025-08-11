package com.xdev.communicator.models.shared.dto;

import com.xdev.communicator.models.common.dtos.BaseDto;
import com.xdev.communicator.models.shared.enums.TypeCategory;


public class BaseTypeDto extends BaseDto {
    private String name; // The name of the type (e.g., "Plastic Waste", "Local SupplierInfo")
    private String description; // Description of the type

    private TypeCategory type;

    public BaseTypeDto() {
    }


    public TypeCategory getType() {
        return type;
    }

    public void setType(TypeCategory type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}