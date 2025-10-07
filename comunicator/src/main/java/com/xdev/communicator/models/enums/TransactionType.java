package com.xdev.communicator.models.enums;

/**
 * Transaction types for the universal financial transaction model
 */
public enum TransactionType {

        RECEPTION_IN,
        TRANSFER_IN,
        SALE,
        EXCHANGE,

    // ==================== PAYMENT TRANSACTIONS ====================
    PAYMENT,

    // ==================== EXPENSE TRANSACTIONS ====================
    EXPENSE,
    PURCHASE,

    // ==================== CREDIT/DEBIT TRANSACTIONS ====================
    CREDIT,
    DEBIT,
    LOAN,
    
    // ==================== TRANSFER TRANSACTIONS ====================
    INTERNAL_TRANSFER,
    
    // ==================== OIL-SPECIFIC TRANSACTIONS ====================
    OIL_SALE,
    OIL_PURCHASE,

    // ==================== WASTE-SPECIFIC TRANSACTIONS ====================
    WASTE_SALE,
    WASTE_PAYMENT,
    WASTE_DISPOSAL_COST,

    // ==================== SUPPLIER/CUSTOMER TRANSACTIONS ====================
    SUPPLIER_PAYMENT,
    SUPPLIER_CREDIT,

    // ==================== BANKING TRANSACTIONS ====================
    DEPOSIT,
    WITHDRAWAL,
    CHECK_DEPOSIT,
    CHECK_PAYMENT


} 