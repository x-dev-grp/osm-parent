package com.xdev.xdevbase.services;

import com.xdev.xdevbase.qr.model.QrResolveResponse;

import java.util.Optional;

public interface GlobalCodeSearchContributor {
    Optional<QrResolveResponse> searchByCode(String code);
}
