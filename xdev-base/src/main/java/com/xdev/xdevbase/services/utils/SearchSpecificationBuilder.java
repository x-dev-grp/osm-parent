package com.xdev.xdevbase.services.utils;

import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.*;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@SuppressWarnings({"unchecked","raw-use"})
public class SearchSpecificationBuilder<T extends BaseEntity> {

    public Specification<T> buildSpecification(SearchModel searchModel) {
        if (searchModel == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchModel.getSearch() != null && !searchModel.getSearch().isEmpty()) {
                for (Map.Entry<String, SearchDetails> entry : searchModel.getSearch().entrySet()) {
                    String key = entry.getKey();
                    SearchDetails details = entry.getValue();

                    if (details != null) {
                        try {
                            handleSearchDetails(key, details, root, criteriaBuilder, predicates);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            if (searchModel.getSearchs() != null && !searchModel.getSearchs().isEmpty()) {
                for (SearchModel nestedModel : searchModel.getSearchs()) {
                    Specification<T> nestedSpec = buildSpecification(nestedModel);
                    if (nestedSpec != null) {
                        Predicate nestedPredicate = nestedSpec.toPredicate(root, query, criteriaBuilder);
                        if (nestedPredicate != null) {
                            predicates.add(nestedPredicate);
                        }
                    }
                }
            }

            if (predicates.isEmpty()) {
                return null;
            } else if (predicates.size() == 1) {
                Predicate result = predicates.getFirst();
                return searchModel.isReverse() ? criteriaBuilder.not(result) : result;
            } else {
                Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
                Predicate combined;

                if (searchModel.getOperation() == SearchOperation.AND) {
                    combined = criteriaBuilder.and(predicateArray);
                } else {
                    combined = criteriaBuilder.or(predicateArray);
                }

                return searchModel.isReverse() ? criteriaBuilder.not(combined) : combined;
            }
        };
    }

    /**
     * Enhanced SearchSpecificationBuilder that properly handles all Java types
     * for predicates in JPA Criteria API
     */
    private void handleSearchDetails(String key, SearchDetails details, Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) throws Exception {
        // Skip processing if all values are null and we're ignoring nulls
        if (allValuesAreNull(details) && details.isIgnoreIfNull()) {
            return;
        }

        Path<?> path = getNestedPropertyPath(root, key);
        Class<?> fieldType = path.getJavaType();

        // Handle equal value
        if (details.getEqualValue() != null) {
            Object convertedValue = convertValueToTargetType(details.getEqualValue(), fieldType);
            if (convertedValue != null) {
                predicates.add(cb.equal(path, convertedValue));
            }
        }

        // Handle like value for String types
        if (details.getLikeValue() != null && String.class.isAssignableFrom(fieldType)) {
            String likeValue = details.getLikeValue().toString();
            predicates.add(cb.like(cb.lower(path.as(String.class)), "%" + likeValue.toLowerCase() + "%"));
        }

        // Handle min value (greater than)
        if (details.getMinValue() != null) {
            handleComparison(cb, path, details.getMinValue(), fieldType, ComparisonType.GREATER_THAN, predicates);
        }

        // Handle min value or equal (greater than or equal)
        if (details.getMinValueOrEqual() != null) {
            handleComparison(cb, path, details.getMinValueOrEqual(), fieldType, ComparisonType.GREATER_THAN_OR_EQUAL, predicates);
        }

        // Handle max value (less than)
        if (details.getMaxValue() != null) {
            handleComparison(cb, path, details.getMaxValue(), fieldType, ComparisonType.LESS_THAN, predicates);
        }

        // Handle max value or equal (less than or equal)
        if (details.getMaxValueOrEqual() != null) {
            handleComparison(cb, path, details.getMaxValueOrEqual(), fieldType, ComparisonType.LESS_THAN_OR_EQUAL, predicates);
        }

        // Handle IN values
        if (details.getInValues() != null && !details.getInValues().isEmpty()) {
            CriteriaBuilder.In<Object> inClause = cb.in(path);
            for (Object value : details.getInValues()) {
                Object convertedValue = convertValueToTargetType(value, fieldType);
                if (convertedValue != null) {
                    inClause.value(convertedValue);
                }
            }
            predicates.add(inClause);
        }

        // Handle NULL/NOT NULL checks
        if (details.getNull() != null) {
            if (details.getNull()) {
                predicates.add(cb.isNull(path));
            } else {
                predicates.add(cb.isNotNull(path));
            }
        }

        // Handle contains value for String types
        if (details.getContainsValue() != null && String.class.isAssignableFrom(fieldType)) {
            String containsValue = details.getContainsValue().toString().toLowerCase();
            predicates.add(cb.like(cb.lower(path.as(String.class)), "%" + containsValue + "%"));
        }
    }

    /**
     * Enum to define the type of comparison operation
     */
    private enum ComparisonType {
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL
    }

    /**
     * Generic method to handle all types of comparisons with proper type handling
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleComparison(
            CriteriaBuilder cb,
            Path<?> path,
            Object value,
            Class<?> fieldType,
            ComparisonType comparisonType,
            List<Predicate> predicates) throws Exception {

        Object convertedValue = convertValueToTargetType(value, fieldType);
        if (convertedValue == null) {
            return;
        }

        // Make sure the value is comparable
        if (!(convertedValue instanceof Comparable)) {
            throw new IllegalArgumentException("Value must be comparable for field type: " + fieldType.getName());
        }

        // For specific common types, use the appropriate typed methods
        if (fieldType == String.class) {
            handleStringComparison(cb, path.as(String.class), (String) convertedValue, comparisonType, predicates);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            handleNumberComparison(cb, path.as(Integer.class), (Integer) convertedValue, comparisonType, predicates);
        } else if (fieldType == Long.class || fieldType == long.class) {
            handleNumberComparison(cb, path.as(Long.class), (Long) convertedValue, comparisonType, predicates);
        } else if (fieldType == Double.class || fieldType == double.class) {
            handleNumberComparison(cb, path.as(Double.class), (Double) convertedValue, comparisonType, predicates);
        } else if (fieldType == Float.class || fieldType == float.class) {
            handleNumberComparison(cb, path.as(Float.class), (Float) convertedValue, comparisonType, predicates);
        } else if (fieldType == BigDecimal.class) {
            handleNumberComparison(cb, path.as(BigDecimal.class), (BigDecimal) convertedValue, comparisonType, predicates);
        } else if (fieldType == LocalDate.class) {
            handleDateComparison(cb, path.as(LocalDate.class), (LocalDate) convertedValue, comparisonType, predicates);
        } else if (fieldType == LocalDateTime.class) {
            handleDateTimeComparison(cb, path.as(LocalDateTime.class), (LocalDateTime) convertedValue, comparisonType, predicates);
        } else if (fieldType == Date.class) {
            handleDateComparison(cb, path.as(Date.class), (Date) convertedValue, comparisonType, predicates);
        } else {
            // For any other type that is Comparable, use the generic approach
            // This might throw type errors in some cases, but it's a fallback
            try {
                Expression<Comparable> comparablePath = (Expression<Comparable>) path;
                Comparable comparableValue = (Comparable) convertedValue;

                switch (comparisonType) {
                    case GREATER_THAN:
                        predicates.add(cb.greaterThan(comparablePath, comparableValue));
                        break;
                    case GREATER_THAN_OR_EQUAL:
                        predicates.add(cb.greaterThanOrEqualTo(comparablePath, comparableValue));
                        break;
                    case LESS_THAN:
                        predicates.add(cb.lessThan(comparablePath, comparableValue));
                        break;
                    case LESS_THAN_OR_EQUAL:
                        predicates.add(cb.lessThanOrEqualTo(comparablePath, comparableValue));
                        break;
                }
            } catch (ClassCastException e) {
                throw new Exception("Failed to create comparison predicate for field type: " + fieldType.getName(), e);
            }
        }
    }

    /**
     * Handle string-specific comparisons
     */
    private void handleStringComparison(
            CriteriaBuilder cb,
            Expression<String> path,
            String value,
            ComparisonType comparisonType,
            List<Predicate> predicates) {

        switch (comparisonType) {
            case GREATER_THAN:
                predicates.add(cb.greaterThan(path, value));
                break;
            case GREATER_THAN_OR_EQUAL:
                predicates.add(cb.greaterThanOrEqualTo(path, value));
                break;
            case LESS_THAN:
                predicates.add(cb.lessThan(path, value));
                break;
            case LESS_THAN_OR_EQUAL:
                predicates.add(cb.lessThanOrEqualTo(path, value));
                break;
        }
    }

    /**
     * Handle number-specific comparisons
     */
    private <N extends Number & Comparable<N>> void handleNumberComparison(
            CriteriaBuilder cb,
            Expression<N> path,
            N value,
            ComparisonType comparisonType,
            List<Predicate> predicates) {

        switch (comparisonType) {
            case GREATER_THAN:
                predicates.add(cb.greaterThan(path, value));
                break;
            case GREATER_THAN_OR_EQUAL:
                predicates.add(cb.greaterThanOrEqualTo(path, value));
                break;
            case LESS_THAN:
                predicates.add(cb.lessThan(path, value));
                break;
            case LESS_THAN_OR_EQUAL:
                predicates.add(cb.lessThanOrEqualTo(path, value));
                break;
        }
    }

    /**
     * Handle LocalDate comparisons
     */
    private void handleDateComparison(
            CriteriaBuilder cb,
            Expression<LocalDate> path,
            LocalDate value,
            ComparisonType comparisonType,
            List<Predicate> predicates) {

        switch (comparisonType) {
            case GREATER_THAN:
                predicates.add(cb.greaterThan(path, value));
                break;
            case GREATER_THAN_OR_EQUAL:
                predicates.add(cb.greaterThanOrEqualTo(path, value));
                break;
            case LESS_THAN:
                predicates.add(cb.lessThan(path, value));
                break;
            case LESS_THAN_OR_EQUAL:
                predicates.add(cb.lessThanOrEqualTo(path, value));
                break;
        }
    }

    /**
     * Handle LocalDateTime comparisons
     */
    private void handleDateTimeComparison(
            CriteriaBuilder cb,
            Expression<LocalDateTime> path,
            LocalDateTime value,
            ComparisonType comparisonType,
            List<Predicate> predicates) {

        switch (comparisonType) {
            case GREATER_THAN:
                predicates.add(cb.greaterThan(path, value));
                break;
            case GREATER_THAN_OR_EQUAL:
                predicates.add(cb.greaterThanOrEqualTo(path, value));
                break;
            case LESS_THAN:
                predicates.add(cb.lessThan(path, value));
                break;
            case LESS_THAN_OR_EQUAL:
                predicates.add(cb.lessThanOrEqualTo(path, value));
                break;
        }
    }

    /**
     * Handle Date comparisons
     */
    private void handleDateComparison(
            CriteriaBuilder cb,
            Expression<Date> path,
            Date value,
            ComparisonType comparisonType,
            List<Predicate> predicates) {

        switch (comparisonType) {
            case GREATER_THAN:
                predicates.add(cb.greaterThan(path, value));
                break;
            case GREATER_THAN_OR_EQUAL:
                predicates.add(cb.greaterThanOrEqualTo(path, value));
                break;
            case LESS_THAN:
                predicates.add(cb.lessThan(path, value));
                break;
            case LESS_THAN_OR_EQUAL:
                predicates.add(cb.lessThanOrEqualTo(path, value));
                break;
        }
    }

    /**
     * Enhanced method to convert values to the target type with better error handling
     */
    private Object convertValueToTargetType(Object value, Class<?> targetType) throws Exception {
        if (value == null || targetType == null) {
            return null;
        }

        // If already the correct type, return as is
        if (targetType.isInstance(value)) {
            return value;
        }

        try {
            // Handle common type conversions
            String stringValue = value.toString();

            // Handle enum types
            if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, stringValue);
            }
            //handle UUID types
            if (targetType == UUID.class) {
                return UUID.fromString(stringValue);
            }

            // Handle date/time types
            if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue);
            }

            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue);
            }

            if (targetType == Date.class) {
                // Try to parse as LocalDate first, then convert to java.sql.Date
                return java.sql.Date.valueOf(LocalDate.parse(stringValue));
            }

            if (targetType == OffsetDateTime.class) {
                try {
                    return OffsetDateTime.parse(stringValue);
                } catch (DateTimeParseException e) {
                    // Try as LocalDateTime and apply system offset
                    LocalDateTime ldt = LocalDateTime.parse(stringValue);
                    return ldt.atOffset(ZoneOffset.systemDefault().getRules().getOffset(ldt));
                }
            }

            // Handle numeric types
            if (Number.class.isAssignableFrom(targetType)) {
                if (targetType == Integer.class) {
                    return Integer.parseInt(stringValue);
                } else if (targetType == Long.class) {
                    return Long.parseLong(stringValue);
                } else if (targetType == Double.class) {
                    return Double.parseDouble(stringValue);
                } else if (targetType == Float.class) {
                    return Float.parseFloat(stringValue);
                } else if (targetType == BigDecimal.class) {
                    return new BigDecimal(stringValue);
                } else if (targetType == Short.class) {
                    return Short.parseShort(stringValue);
                } else if (targetType == Byte.class) {
                    return Byte.parseByte(stringValue);
                }
            }

            // Handle boolean
            if (targetType == Boolean.class) {
                return Boolean.parseBoolean(stringValue);
            }

            // Handle character (use first char of string)
            if (targetType == Character.class && !stringValue.isEmpty()) {
                return stringValue.charAt(0);
            }

            // If we get here and the target is a primitive wrapper, try to use valueOf
            try {
                Method valueOfMethod = targetType.getMethod("valueOf", String.class);
                if (valueOfMethod != null) {
                    return valueOfMethod.invoke(null, stringValue);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Ignore and fall through to the exception
            }

        } catch (Exception e) {
            throw new Exception("Failed to convert value [" + value + "] to type [" + targetType.getName() + "]: " + e.getMessage());
        }

        throw new Exception("Unsupported conversion from [" + value.getClass().getName() + "] to [" + targetType.getName() + "]");
    }

    /**
     * Helper method to check if all searchable values in a SearchDetails object are null
     */
    private boolean allValuesAreNull(SearchDetails details) {
        return details.getEqualValue() == null &&
                details.getLikeValue() == null &&
                details.getMinValue() == null &&
                details.getMinValueOrEqual() == null &&
                details.getMaxValue() == null &&
                details.getMaxValueOrEqual() == null &&
                details.getNull() == null &&
                (details.getInValues() == null || details.getInValues().isEmpty()) &&
                details.getContainsValue() == null;
    }

    /**
     * Helper method to get property paths that may be nested
     */
    private Path<?> getNestedPropertyPath(Root<T> root, String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        Path<?> path = root;

        for (String part : parts) {
            path = path.get(part);
        }

        return path;
    }
}