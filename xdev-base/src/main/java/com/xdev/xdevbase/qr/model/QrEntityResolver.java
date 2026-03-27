package com.xdev.xdevbase.qr.model;


import com.xdev.xdevbase.qr.model.QrResolveResponse;

public interface QrEntityResolver {
    String getEntityType();
    QrResolveResponse resolve(String publicCode);
}