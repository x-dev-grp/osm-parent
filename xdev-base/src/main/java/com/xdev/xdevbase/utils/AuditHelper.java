package com.xdev.xdevbase.utils;

 import com.xdev.xdevbase.config.TenantContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AuditHelper {


    // ---------- helpers ----------

    @SuppressWarnings("unchecked")
    private static Optional<String> currentUserId() {
        try {
            Optional<Map<String, Object>> osmUserOpt = SecurityUtils.getCurrentOsmUser();
            if (osmUserOpt.isEmpty()) return Optional.empty();
            Map<String, Object> osmUser = osmUserOpt.get();
            Object id = osmUser.get("id");
            if (id != null) return Optional.of(String.valueOf(id));
            Object externalId = osmUser.get("externalId");
            if (externalId != null) return Optional.of(String.valueOf(externalId));
        } catch (Exception ignored) {}
        return Optional.empty();
    }


    private static Object convertValueForField(Field field, Object value) {
        if (value == null) return null;

        Class<?> type = field.getType();

        // UUID handling
        if (type == UUID.class && !(value instanceof UUID)) {
            try { return UUID.fromString(String.valueOf(value)); } catch (Exception ignored) {}
        }

        // Instant / LocalDateTime conversions
        if (type == Instant.class && value instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        if (type == LocalDateTime.class && value instanceof Instant inst) {
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        }

        // Default: return as-is
        return value;
    }
    private AuditHelper() {}

    public static <E> void applyAuditOnCreate(E entity) {
        currentUserId().ifPresent(uid -> {
            setFieldIfNull(entity, "createdBy", uid);
            setField(entity, "lastModifiedBy", uid);
        });

        setFieldIfNull(entity, "createdDate", Instant.now());
        setField(entity, "lastModifiedDate", Instant.now());

        // tenant only once at creation
        setFieldIfNull(entity, "tenantId", TenantContext.getCurrentTenant());
    }

    public static <E> void applyAuditOnUpdate(E entity) {
        currentUserId().ifPresent(uid -> setField(entity, "lastModifiedBy", uid));
        setField(entity, "lastModifiedDate", Instant.now());
        // DO NOT touch createdDate here
    }

    // ---------- helpers ----------

    private static void setFieldIfNull(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) return;
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        if (field == null) return;
        ReflectionUtils.makeAccessible(field);
        try {
            Object current = field.get(target);
            if (current == null) {
                ReflectionUtils.setField(field, target, convertValueForField(field, value));
            }
        } catch (IllegalAccessException ignored) {}
    }

    private static void setField(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) return;
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        if (field == null) return;
        ReflectionUtils.makeAccessible(field);
        try {
            ReflectionUtils.setField(field, target, convertValueForField(field, value));
        } catch (IllegalArgumentException ignored) {}
    }
}
