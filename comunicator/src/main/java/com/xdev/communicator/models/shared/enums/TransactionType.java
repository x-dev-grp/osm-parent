package com.xdev.communicator.models.shared.enums;

/**
 * Transaction types for the universal financial transaction model
 */
public enum TransactionType {
    
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

    // ==================== SUPPLIER/CUSTOMER TRANSACTIONS ====================
    SUPPLIER_PAYMENT,
    SUPPLIER_CREDIT,

    // ==================== BANKING TRANSACTIONS ====================
    DEPOSIT,
    WITHDRAWAL,
    CHECK_DEPOSIT,
    CHECK_PAYMENT


} 