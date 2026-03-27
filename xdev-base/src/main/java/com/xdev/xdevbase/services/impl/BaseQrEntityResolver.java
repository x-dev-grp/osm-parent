package com.xdev.xdevbase.services.impl;

import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.qr.model.QrEntityResolver;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;

public abstract class BaseQrEntityResolver<E extends BaseEntity> implements QrEntityResolver {

    protected final BaseRepository<E> repository;

    public BaseQrEntityResolver(BaseRepository<E> repository) {
        this.repository = repository;
    }

    @Override
    public QrResolveResponse resolve(String publicCode) {
        // Common: find entity by QR code
        E entity = repository.findByQrHex(publicCode)
                .orElseThrow(() -> new RuntimeException("Entity not found for code: " + publicCode));

        // Build response with common fields

        return getQrResolveResponse(publicCode, entity);
    }

    private QrResolveResponse getQrResolveResponse(String publicCode, E entity) {
        QrResolveResponse response = new QrResolveResponse();
        response.setEntityType(getEntityType());
        response.setPublicCode(publicCode);
        response.setEntityId(entity.getId().toString());

        // Delegate entity-specific fields
        response.setLabel(getLabel(entity));
        response.setStatus(getStatus(entity));
        response.setMobileRoute(getMobileRoute());
        response.setData(getData(entity));
        return response;
    }

    protected abstract String getLabel(E entity);
    protected abstract String getStatus(E entity);
    protected abstract String getMobileRoute();
    protected Object getData(E entity) { return null; } // optional, override if needed
}