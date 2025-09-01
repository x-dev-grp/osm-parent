package com.xdev.communicator.models.shared.enums;

/**
 * Shared enum for waste types across microservices
 * Used for consistent waste categorization between Production and Finance services
 */
public enum WasteType {
    /**
     * Margine - liquid waste from oil extraction
     */
    MARGINE,
    
    /**
     * Pomace - solid waste from oil extraction
     */
    POMACE,
    
    /**
     * Vegetal solids - plant-based solid waste
     */
    VEGETAL_SOLIDS,
    
    /**
     * Other types of waste not categorized above
     */
    OTHER
}