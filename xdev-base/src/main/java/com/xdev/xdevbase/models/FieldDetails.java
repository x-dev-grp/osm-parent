package com.xdev.xdevbase.models;

import java.util.HashMap;
import java.util.Map;

public class FieldDetails {
    private String name;
    private String label;
    private boolean isEnumValue;
    private Map<String,String> enumValues = new HashMap<String,String>();

    public boolean isEnumValue() {
        return isEnumValue;
    }

    public void setEnumValue(boolean enumValue) {
        isEnumValue = enumValue;
    }

    public Map<String, String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
