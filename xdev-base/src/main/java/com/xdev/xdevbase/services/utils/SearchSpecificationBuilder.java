package com.xdev.xdevbase.services.utils;

import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.SearchDetails;
import com.xdev.xdevbase.models.SearchModel;
import com.xdev.xdevbase.models.SearchOperation;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
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
@SuppressWarnings({"unchecked", "raw-use"})
public class SearchSpecificationBuilder<T extends BaseEntity> {

    public Specification<T> buildSpecification(SearchModel searchModel) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "buildSpecification", searchModel);

        try {
            if (searchModel == null) {
                OSMLogger.logMethodExit(this.getClass(), "buildSpecification", "SearchModel is null, returning null specification");
                return null;
            }

            OSMLogger.logBusinessEvent(this.getClass(), "SEARCH_SPECIFICATION_BUILD_START",
                    "Building specification for search model with " +
                            (searchModel.getSearch() != null ? searchModel.getSearch().size() : 0) + " search criteria");

            Specification<T> specification = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Process search criteria
                if (searchModel.getSearch() != null && !searchModel.getSearch().isEmpty()) {
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_CRITERIA_PROCESSING",
                            "Processing " + searchModel.getSearch().size() + " search criteria");

                    for (Map.Entry<String, SearchDetails> entry : searchModel.getSearch().entrySet()) {
                        String key = entry.getKey();
                        SearchDetails details = entry.getValue();

                        if (details != null) {
                            try {
                                OSMLogger.logDataAccess(this.getClass(), "SEARCH_DETAIL_PROCESSING",
                                        "Processing search detail for field: " + key);
                                handleSearchDetails(key, details, root, criteriaBuilder, predicates);
                            } catch (Exception e) {
                                OSMLogger.logException(this.getClass(),
                                        "Error processing search details for field: " + key, e);
                                throw new RuntimeException("Failed to process search details for field: " + key, e);
                            }
                        }
                    }
                }

                // Process nested search models
                if (searchModel.getSearchs() != null && !searchModel.getSearchs().isEmpty()) {
                    OSMLogger.logDataAccess(this.getClass(), "NESTED_SEARCH_PROCESSING",
                            "Processing " + searchModel.getSearchs().size() + " nested search models");

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

                // Build final predicate
                if (predicates.isEmpty()) {
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_PREDICATE_RESULT",
                            "No predicates generated, returning null");
                    return null;
                } else if (predicates.size() == 1) {
                    Predicate result = predicates.getFirst();
                    Predicate finalResult = searchModel.isReverse() ? criteriaBuilder.not(result) : result;
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_PREDICATE_RESULT",
                            "Single predicate generated, reverse: " + searchModel.isReverse());
                    return finalResult;
                } else {
                    Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
                    Predicate combined;

                    if (searchModel.getOperation() == SearchOperation.AND) {
                        combined = criteriaBuilder.and(predicateArray);
                    } else {
                        combined = criteriaBuilder.or(predicateArray);
                    }

                    Predicate finalResult = searchModel.isReverse() ? criteriaBuilder.not(combined) : combined;
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_PREDICATE_RESULT",
                            "Combined " + predicates.size() + " predicates with operation: " +
                                    searchModel.getOperation() + ", reverse: " + searchModel.isReverse());
                    return finalResult;
                }
            };

            OSMLogger.logMethodExit(this.getClass(), "buildSpecification", "Specification built successfully");
            OSMLogger.logPerformance(this.getClass(), "buildSpecification", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "SEARCH_SPECIFICATION_BUILD_COMPLETE",
                    "Search specification built successfully with " +
                            (searchModel.getSearch() != null ? searchModel.getSearch().size() : 0) + " criteria");

            return specification;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error building search specification", e);
            throw e;
        }
    }

    /**
     * Enhanced SearchSpecificationBuilder that properly handles all Java types
     * for predicates in JPA Criteria API
     */
    private void handleSearchDetails(String key, SearchDetails details, Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) throws Exception {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "handleSearchDetails", "Field: " + key);

        try {
            // Skip processing if all values are null and we're ignoring nulls
            if (allValuesAreNull(details) && details.isIgnoreIfNull()) {
                OSMLogger.logDataAccess(this.getClass(), "SEARCH_DETAIL_SKIPPED",
                        "Skipping search detail for field: " + key + " (all values null and ignoreIfNull=true)");
                return;
            }

            Path<?> path = getNestedPropertyPath(root, key);
            Class<?> fieldType = path.getJavaType();

            OSMLogger.logDataAccess(this.getClass(), "SEARCH_FIELD_TYPE",
                    "Field: " + key + ", Type: " + fieldType.getSimpleName());

            // Handle equal value
            if (details.getEqualValue() != null) {
                Object convertedValue = convertValueToTargetType(details.getEqualValue(), fieldType);
                if (convertedValue != null) {
                    predicates.add(cb.equal(path, convertedValue));
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_EQUAL_PREDICATE",
                            "Added equal predicate for field: " + key + ", value: " + convertedValue);
                }
            }

            // Handle like value for String types
            if (details.getLikeValue() != null && String.class.isAssignableFrom(fieldType)) {
                String likeValue = details.getLikeValue().toString();
                predicates.add(cb.like(cb.lower(path.as(String.class)), "%" + likeValue.toLowerCase() + "%"));
                OSMLogger.logDataAccess(this.getClass(), "SEARCH_LIKE_PREDICATE",
                        "Added like predicate for field: " + key + ", value: " + likeValue);
            }

            // Handle min value (greater than)
            if (details.getMinValue() != null) {
                handleComparison(cb, path, details.getMinValue(), fieldType, ComparisonType.GREATER_THAN, predicates, key);
            }

            // Handle min value or equal (greater than or equal)
            if (details.getMinValueOrEqual() != null) {
                handleComparison(cb, path, details.getMinValueOrEqual(), fieldType, ComparisonType.GREATER_THAN_OR_EQUAL, predicates, key);
            }

            // Handle max value (less than)
            if (details.getMaxValue() != null) {
                handleComparison(cb, path, details.getMaxValue(), fieldType, ComparisonType.LESS_THAN, predicates, key);
            }

            // Handle max value or equal (less than or equal)
            if (details.getMaxValueOrEqual() != null) {
                handleComparison(cb, path, details.getMaxValueOrEqual(), fieldType, ComparisonType.LESS_THAN_OR_EQUAL, predicates, key);
            }

            // Handle IN values
            if (details.getInValues() != null && !details.getInValues().isEmpty()) {
                CriteriaBuilder.In<Object> inClause = cb.in(path);
                int validValues = 0;
                for (Object value : details.getInValues()) {
                    Object convertedValue = convertValueToTargetType(value, fieldType);
                    if (convertedValue != null) {
                        inClause.value(convertedValue);
                        validValues++;
                    }
                }
                if (validValues > 0) {
                    predicates.add(inClause);
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_IN_PREDICATE",
                            "Added IN predicate for field: " + key + " with " + validValues + " values");
                }
            }

            // Handle NULL/NOT NULL checks
            if (details.getNull() != null) {
                if (details.getNull()) {
                    predicates.add(cb.isNull(path));
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_NULL_PREDICATE",
                            "Added IS NULL predicate for field: " + key);
                } else {
                    predicates.add(cb.isNotNull(path));
                    OSMLogger.logDataAccess(this.getClass(), "SEARCH_NOT_NULL_PREDICATE",
                            "Added IS NOT NULL predicate for field: " + key);
                }
            }

            // Handle contains value for String types
            if (details.getContainsValue() != null && String.class.isAssignableFrom(fieldType)) {
                String containsValue = details.getContainsValue().toString().toLowerCase();
                predicates.add(cb.like(cb.lower(path.as(String.class)), "%" + containsValue + "%"));
                OSMLogger.logDataAccess(this.getClass(), "SEARCH_CONTAINS_PREDICATE",
                        "Added contains predicate for field: " + key + ", value: " + containsValue);
            }

            OSMLogger.logMethodExit(this.getClass(), "handleSearchDetails",
                    "Processed search details for field: " + key + ", added " + predicates.size() + " predicates");
            OSMLogger.logPerformance(this.getClass(), "handleSearchDetails", startTime, System.currentTimeMillis());

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error handling search details for field: " + key, e);
            throw e;
        }
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
            List<Predicate> predicates,
            String fieldName) throws Exception {

        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "handleComparison",
                "Field: " + fieldName + ", Type: " + comparisonType + ", Value: " + value);

        try {
            Object convertedValue = convertValueToTargetType(value, fieldType);
            if (convertedValue == null) {
                OSMLogger.logDataAccess(this.getClass(), "COMPARISON_SKIPPED",
                        "Skipping comparison for field: " + fieldName + " (converted value is null)");
                return;
            }

            // Make sure the value is comparable
            if (!(convertedValue instanceof Comparable)) {
                String errorMsg = "Value must be comparable for field type: " + fieldType.getName();
                OSMLogger.logException(this.getClass(), errorMsg,
                        new IllegalArgumentException(errorMsg));
                throw new IllegalArgumentException(errorMsg);
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
                    String errorMsg = "Failed to create comparison predicate for field type: " + fieldType.getName();
                    OSMLogger.logException(this.getClass(), errorMsg, e);
                    throw new Exception(errorMsg, e);
                }
            }

            OSMLogger.logDataAccess(this.getClass(), "COMPARISON_PREDICATE_ADDED",
                    "Added " + comparisonType + " predicate for field: " + fieldName + ", value: " + convertedValue);
            OSMLogger.logMethodExit(this.getClass(), "handleComparison",
                    "Comparison completed for field: " + fieldName);
            OSMLogger.logPerformance(this.getClass(), "handleComparison", startTime, System.currentTimeMillis());

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error handling comparison for field: " + fieldName + ", type: " + comparisonType, e);
            throw e;
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
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "convertValueToTargetType",
                "Value: " + value + ", TargetType: " + targetType.getSimpleName());

        try {
            if (value == null || targetType == null) {
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SKIPPED",
                        "Skipping conversion (value or targetType is null)");
                return null;
            }

            // If already the correct type, return as is
            if (targetType.isInstance(value)) {
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SKIPPED",
                        "Value already of correct type: " + targetType.getSimpleName());
                return value;
            }

            String stringValue = value.toString();

            // Handle enum types
            if (targetType.isEnum()) {
                Object result = Enum.valueOf((Class<Enum>) targetType, stringValue);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to enum: " + result);
                return result;
            }

            //handle UUID types
            if (targetType == UUID.class) {
                Object result = UUID.fromString(stringValue);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to UUID: " + result);
                return result;
            }

            // Handle date/time types
            if (targetType == LocalDate.class) {
                Object result = LocalDate.parse(stringValue);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to LocalDate: " + result);
                return result;
            }

            if (targetType == LocalDateTime.class) {
                Object result = LocalDateTime.parse(stringValue);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to LocalDateTime: " + result);
                return result;
            }

            if (targetType == Date.class) {
                // Try to parse as LocalDate first, then convert to java.sql.Date
                Object result = java.sql.Date.valueOf(LocalDate.parse(stringValue));
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to Date: " + result);
                return result;
            }

            if (targetType == OffsetDateTime.class) {
                try {
                    Object result = OffsetDateTime.parse(stringValue);
                    OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                            "Converted to OffsetDateTime: " + result);
                    return result;
                } catch (DateTimeParseException e) {
                    // Try as LocalDateTime and apply system offset
                    LocalDateTime ldt = LocalDateTime.parse(stringValue);
                    Object result = ldt.atOffset(ZoneOffset.systemDefault().getRules().getOffset(ldt));
                    OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                            "Converted to OffsetDateTime (with system offset): " + result);
                    return result;
                }
            }

            // Handle numeric types
            if (Number.class.isAssignableFrom(targetType)) {
                Object result = null;
                if (targetType == Integer.class) {
                    result = Integer.parseInt(stringValue);
                } else if (targetType == Long.class) {
                    result = Long.parseLong(stringValue);
                } else if (targetType == Double.class) {
                    result = Double.parseDouble(stringValue);
                } else if (targetType == Float.class) {
                    result = Float.parseFloat(stringValue);
                } else if (targetType == BigDecimal.class) {
                    result = new BigDecimal(stringValue);
                } else if (targetType == Short.class) {
                    result = Short.parseShort(stringValue);
                } else if (targetType == Byte.class) {
                    result = Byte.parseByte(stringValue);
                }

                if (result != null) {
                    OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                            "Converted to " + targetType.getSimpleName() + ": " + result);
                    return result;
                }
            }

            // Handle boolean
            if (targetType == Boolean.class) {
                Object result = Boolean.parseBoolean(stringValue);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to Boolean: " + result);
                return result;
            }

            // Handle character (use first char of string)
            if (targetType == Character.class && !stringValue.isEmpty()) {
                Object result = stringValue.charAt(0);
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                        "Converted to Character: " + result);
                return result;
            }

            // If we get here and the target is a primitive wrapper, try to use valueOf
            try {
                Method valueOfMethod = targetType.getMethod("valueOf", String.class);
                if (valueOfMethod != null) {
                    Object result = valueOfMethod.invoke(null, stringValue);
                    OSMLogger.logDataAccess(this.getClass(), "CONVERSION_SUCCESS",
                            "Converted using valueOf method: " + result);
                    return result;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Ignore and fall through to the exception
                OSMLogger.logDataAccess(this.getClass(), "CONVERSION_VALUE_OF_FAILED",
                        "valueOf method failed for type: " + targetType.getSimpleName());
            }

            String errorMsg = "Unsupported conversion from [" + value.getClass().getName() + "] to [" + targetType.getName() + "]";
            OSMLogger.logException(this.getClass(), errorMsg, new Exception(errorMsg));
            throw new Exception(errorMsg);

        } catch (Exception e) {
            String errorMsg = "Failed to convert value [" + value + "] to type [" + targetType.getName() + "]: " + e.getMessage();
            OSMLogger.logException(this.getClass(), errorMsg, e);
            throw new Exception(errorMsg, e);
        } finally {
            OSMLogger.logPerformance(this.getClass(), "convertValueToTargetType", startTime, System.currentTimeMillis());
        }
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
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getNestedPropertyPath", "PropertyPath: " + propertyPath);

        try {
            String[] parts = propertyPath.split("\\.");
            Path<?> path = root;

            for (String part : parts) {
                path = path.get(part);
            }

            OSMLogger.logMethodExit(this.getClass(), "getNestedPropertyPath",
                    "Resolved path for: " + propertyPath + " with " + parts.length + " parts");
            OSMLogger.logPerformance(this.getClass(), "getNestedPropertyPath", startTime, System.currentTimeMillis());

            return path;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error resolving nested property path: " + propertyPath, e);
            throw e;
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
}