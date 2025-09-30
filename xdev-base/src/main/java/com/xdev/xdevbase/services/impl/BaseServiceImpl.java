package com.xdev.xdevbase.services.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.xdevbase.apiDTOs.SearchResponse;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.*;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.AuditHelper;
import com.xdev.xdevbase.services.utils.SearchSpecificationBuilder;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unchecked")
public abstract class  BaseServiceImpl<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> implements BaseService<E, INDTO, OUTDTO> {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_RECORDS_PER_DOCUMENT = 1000;
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
    public static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    protected final BaseRepository<E> repository;
    protected final ModelMapper modelMapper;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<Object, Object> lMap = new ConcurrentHashMap<>();
    protected Class<E> entityClass;
    protected Class<INDTO> inDTOClass;
    protected Class<OUTDTO> outDTOClass;
    @Autowired
    private SearchSpecificationBuilder<E> specificationBuilder;

    private ExportDetails currentExportDetails;

    protected BaseServiceImpl(BaseRepository<E> repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.entityClass = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.inDTOClass = (Class<INDTO>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.outDTOClass = (Class<OUTDTO>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    }

    @Override
    public Class<E> getEntityClass() {
        return this.entityClass;
    }

    @Override
    public Class<INDTO> getInDTOClass() {
        return this.inDTOClass;
    }

    @Override
    public Class<OUTDTO> getOutDTOClass() {
        return this.outDTOClass;
    }

    @Override
    public OUTDTO findById(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findById", id);

        try {
            Optional<E> data = repository.findByIdAndIsDeletedFalse(id);
            if (data.isEmpty()) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity not found with ID: {}", id);
                throw new EntityNotFoundException("Entity not found with this id " + id);
            } else {
                OUTDTO result = modelMapper.map(data.get(), outDTOClass);
                OSMLogger.logMethodExit(this.getClass(), "findById", result);
                OSMLogger.logPerformance(this.getClass(), "findById", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "READ", entityClass.getSimpleName());
                return result;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding entity by ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<OUTDTO> findAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll");

        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            List<E> data = repository.findAllByTenantIdAndIsDeletedFalse(tenantId);
            List<OUTDTO> result = data.stream().map(item -> modelMapper.map(item, outDTOClass)).toList();
            OSMLogger.logMethodExit(this.getClass(), "findAll", "Found " + result.size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_ALL", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding all entities", e);
            throw e;
        }
    }

    @Override
    public Page<OUTDTO> findAll(int page, int size, String sort, String direction) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll", page, size, sort, direction);

        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);  // "ASC" or "DESC"
            Sort sortObject = Sort.by(sortDirection, sort);  // Sort by the field and direction
            Pageable pageable = PageRequest.of(page, size, sortObject);
            Page<E> data = repository.findAllByTenantIdAndIsDeletedFalse(tenantId,pageable);

            Page<OUTDTO> result = data.map(item -> modelMapper.map(item, outDTOClass));
            OSMLogger.logMethodExit(this.getClass(), "findAll", "Page " + page + " with " + result.getContent().size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_PAGEABLE", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding entities with pagination", e);
            throw e;
        }
    }

    @Override
    public OUTDTO save(INDTO request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", request);

        try {
            if (request == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Save request is null");
                return null;
            } else {
                E entity = this.modelMapper.map(request, this.entityClass);
                resolveEntityRelations(entity);
                AuditHelper.applyAuditOnCreate(entity);

                E savedEntity = this.repository.save(entity);
                OUTDTO result = this.modelMapper.map(savedEntity, this.outDTOClass);

                OSMLogger.logMethodExit(this.getClass(), "save", result);
                OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "CREATE", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_SAVED", "Saved entity with ID: " + savedEntity.getId());

                return result;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error saving entity", e);
            throw e;
        }
    }

    @Override
    public List<OUTDTO> save(List<INDTO> request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", "List with " + (request != null ? request.size() : 0) + " items");

        try {
            if (request != null && !request.isEmpty()) {
                List<E> entities = request.stream()
                        .map((item) -> this.modelMapper.map(item, this.entityClass))
                        .toList();

                entities.forEach(this::resolveEntityRelations);

                entities = this.repository.saveAll(entities);
                List<OUTDTO> result = entities.stream()
                        .map(item -> {
                            AuditHelper.applyAuditOnCreate(item);  // side effect
                            return this.modelMapper.map(item, this.outDTOClass); // return value
                        })
                        .toList();

                OSMLogger.logMethodExit(this.getClass(), "save", "Saved " + result.size() + " entities");
                OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "CREATE_BATCH", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITIES_SAVED", "Saved " + result.size() + " entities");

                return result;
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Save request list is empty or null");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error saving entities", e);
            throw e;
        }
    }

    @Override
    public OUTDTO update(INDTO request) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "update", request);

        try {
            if (request != null && request.getId() != null) {
                Optional<E> existedOptEntity = this.repository.findById(request.getId());
                if (existedOptEntity.isEmpty()) {
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity with ID {} not found for update", request.getId());
                    return null;
                } else {
                    E existedEntity = existedOptEntity.get();
                    AuditHelper.applyAuditOnCreate(existedEntity);
                    UUID externalId=existedEntity.getExternalId();// side effect
                    this.modelMapper.map(request, existedEntity);
                    existedEntity.setExternalId(externalId);
                    resolveEntityRelations(existedEntity);

                    E updatedEntity = this.repository.save(existedEntity);
                    OUTDTO result = this.modelMapper.map(updatedEntity, this.outDTOClass);

                    OSMLogger.logMethodExit(this.getClass(), "update", result);
                    OSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
                    OSMLogger.logDataAccess(this.getClass(), "UPDATE", entityClass.getSimpleName());
                    OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_UPDATED", "Updated entity with ID: " + updatedEntity.getId());

                    return result;
                }
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Update request or ID is null: {}", request);
                return null;
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error updating entity", e);
            throw e;
        }
    }

    @Override
    public void resolveEntityRelations(E entity) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "resolveEntityRelations", entity);

        try {
            // This method is meant to be overridden by subclasses
            // Default implementation does nothing
            OSMLogger.logMethodExit(this.getClass(), "resolveEntityRelations");
            OSMLogger.logPerformance(this.getClass(), "resolveEntityRelations", startTime, System.currentTimeMillis());
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error resolving entity relations", e);
            throw e;
        }
    }


    @Override
    public void remove(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "remove", id);

        try {
            if (id != null) {
                repository.deleteById(id);
                OSMLogger.logMethodExit(this.getClass(), "remove");
                OSMLogger.logPerformance(this.getClass(), "remove", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "REMOVE", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_REMOVED", "Removed entity with ID: " + id);
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Remove ID is null");
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error removing entity with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public OUTDTO delete(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "delete", id);

        try {
            if (id == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Delete ID is null: {}", id);
                return null;
            }
            E entity = repository.findById(id).orElse(null);
            if (entity == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity with ID {} not found for deletion", id);
                return null;
            }

            entity.setDeleted(true);
            E updatedEntity = repository.save(entity);
            OUTDTO result = modelMapper.map(updatedEntity, outDTOClass);

            OSMLogger.logMethodExit(this.getClass(), "delete", result);
            OSMLogger.logPerformance(this.getClass(), "delete", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "DELETE", entityClass.getSimpleName());
            OSMLogger.logBusinessEvent(this.getClass(), "ENTITY_DELETED", "Deleted entity with ID: " + id);

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error deleting entity with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public void removeAll(Collection<INDTO> entities) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "removeAll", "Collection with " + (entities != null ? entities.size() : 0) + " items");

        try {
            if (entities != null) {
                List<E> entitiesToDelete = entities.stream().map(item -> modelMapper.map(item, entityClass)).toList();
                repository.deleteAll(entitiesToDelete);
                OSMLogger.logMethodExit(this.getClass(), "removeAll");
                OSMLogger.logPerformance(this.getClass(), "removeAll", startTime, System.currentTimeMillis());
                OSMLogger.logDataAccess(this.getClass(), "REMOVE_ALL", entityClass.getSimpleName());
                OSMLogger.logBusinessEvent(this.getClass(), "ENTITIES_REMOVED", "Removed " + entitiesToDelete.size() + " entities");
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entities to delete are null");
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error removing entities", e);
            throw e;
        }
    }


    @Override
    public Optional<Revision<Integer, E>> findLastRevisionById(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findLastRevisionById", id);

        try {
            Optional<Revision<Integer, E>> data = this.repository.findLastChangeRevision(id);
            OSMLogger.logMethodExit(this.getClass(), "findLastRevisionById", data.isPresent() ? "Found revision" : "No revision found");
            OSMLogger.logPerformance(this.getClass(), "findLastRevisionById", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_LAST_REVISION", entityClass.getSimpleName());
            return data;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding last revision for ID: " + id, e);
            throw e;
        }
    }

    @Override
    public Revisions<Integer, E> findRevisionsById(UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findRevisionsById", id);

        try {
            Revisions<Integer, E> data = this.repository.findRevisions(id);
            OSMLogger.logMethodExit(this.getClass(), "findRevisionsById", "Found " + data.getContent().size() + " revisions");
            OSMLogger.logPerformance(this.getClass(), "findRevisionsById", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_REVISIONS", entityClass.getSimpleName());
            return data;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding revisions for ID: " + id, e);
            throw e;
        }
    }

    @Override
    public SearchResponse<E, OUTDTO> search(SearchData searchData) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "search", searchData);

        try {
            int page = searchData.getPage() != null ? searchData.getPage() : 0;
            int size = searchData.getSize() != null ? searchData.getSize() : 10;
            Sort.Direction direction = (searchData.getOrder() != null && searchData.getOrder().equalsIgnoreCase("DESC")) ? Sort.Direction.DESC : Sort.Direction.ASC;
            String sort = searchData.getSort() != null ? searchData.getSort() : "createdDate";
            Pageable pageable = PageRequest.of(page, size, direction, sort);
             if(searchData.isFilterTenant()) {
                 SearchDetails details = new SearchDetails();
                 details.setEqualValue(TenantContext.getCurrentTenant());
                 if(searchData.getSearchData() != null) {
                     searchData.getSearchData().getSearch().put("tenantId",details);
                 }
             }
            Specification<E> spec = null;
            if (searchData.getSearchData() != null) {
                spec = specificationBuilder.buildSpecification(searchData.getSearchData());
            }

            Page<E> result;
            if (spec != null) {
                result = repository.findAll(spec, pageable);
            } else {
                if(searchData.isFilterTenant()){
                    result = repository.findAllByTenantIdAndIsDeletedFalse(TenantContext.getCurrentTenant(),pageable);
                }else
                     result = repository.findAllByIsDeletedFalse(pageable);
            }
            List<OUTDTO> dtos = result.getContent().stream().map(
                    element -> modelMapper.map(element, outDTOClass)
            ).toList();

            SearchResponse<E, OUTDTO> response = new SearchResponse<>(
                    result.getTotalElements(),
                    dtos,
                    result.getTotalPages(),
                    result.getNumber() + 1
            );

            OSMLogger.logMethodExit(this.getClass(), "search", "Found " + dtos.size() + " entities out of " + result.getTotalElements());
            OSMLogger.logPerformance(this.getClass(), "search", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "SEARCH", entityClass.getSimpleName());

            return response;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error during search operation", e);
            return new SearchResponse<>(
                    0,
                    null,
                    0,
                    0
            );
        }
    }


//    public byte[] exportToPdf(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToPdf", exportDetails);
//
//        try {
//            if(exportDetails.getSearchData().isFilterTenant()) {
//                SearchDetails details = new SearchDetails();
//                details.setEqualValue(TenantContext.getCurrentTenant());
//                if(exportDetails.getSearchData().getSearchData() != null) {
//                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId",details);
//                }
//            }
//            // Get total count first to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // If total records exceed maximum per document, create multiple PDFs
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultiplePdfs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple PDFs for {} records", totalRecords);
//            } else {
//                result = createSinglePdf(exportDetails.getSearchData(), exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single PDF for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToPdf", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToPdf", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED",
//                    "PDF generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating PDF export", e);
//            throw e;
//        }
//    }

    public byte[] exportToPdf(ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportToPdf", exportDetails);

        try {
            this.currentExportDetails = exportDetails; // Store for dynamic field processing

            if(exportDetails.getSearchData().isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if(exportDetails.getSearchData().getSearchData() != null) {
                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
                }
            }

            // Get total count first to determine if pagination is needed
            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
            SearchResponse<E, OUTDTO> countResponse = search(countData);
            long totalRecords = countResponse.getTotal();

            // Get sample data to process dynamic columns
            SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
            sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
            SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

            // Process collection fields and generate complete field list
            List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
            exportDetails.setFieldDetails(allFields);

            byte[] result;
            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
                result = createMultiplePdfs(exportDetails.getSearchData(), totalRecords, allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple PDFs for {} records", totalRecords);
            } else {
                result = createSinglePdf(exportDetails.getSearchData(), allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single PDF for {} records", totalRecords);
            }

            OSMLogger.logMethodExit(this.getClass(), "exportToPdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "exportToPdf", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED",
                    "PDF generated for " + totalRecords + " records (" + result.length + " bytes)");

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating PDF export", e);
            throw e;
        } finally {
            this.currentExportDetails = null; // Clear after use
        }
    }
    /**
     * Create a single PDF document
     *
     * @param searchData     search criteria
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[] createSinglePdf(SearchData searchData, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSinglePdf", "fileName: " + fileName + ", fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.logDataAccess(this.getClass(), "PDF_DATA_RETRIEVED", entityClass.getSimpleName());

            byte[] result = generatePdf(data, 1, 1, fieldsToExport, fileName);

            OSMLogger.logMethodExit(this.getClass(), "createSinglePdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSinglePdf", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single PDF", e);
            throw e;
        }
    }

    /**
     * Create multiple PDF documents in a ZIP archive
     *
     * @param searchData     search criteria
     * @param totalRecords   total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultiplePdfs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultiplePdfs",
                "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_PDF_START",
                    "Creating " + totalPages + " PDF files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.logDataAccess(this.getClass(), "PDF_PAGE_DATA_RETRIEVED",
                            entityClass.getSimpleName());

                    // Generate PDF for current page
                    byte[] pdfData = generatePdf(data, pageNum + 1, totalPages, fieldsToExport, fileName);

                    // Add PDF to ZIP archive
                    ZipEntry entry = new ZipEntry(fileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".pdf");
                    zipStream.putNextEntry(entry);
                    zipStream.write(pdfData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "PDF_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "PDF_PAGE_COMPLETED",
                            "Completed PDF page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultiplePdfs", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultiplePdfs", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_PDF_COMPLETED",
                    "Successfully created " + totalPages + " PDF files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for PDFs", e);
            throw new RuntimeException("Failed to create PDF export", e);
        }
    }

    /**
     * Generate a PDF document for the given data
     *
     * @param data           list of entities
     * @param partNumber     current part number
     * @param totalParts     total number of parts
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[]
    generatePdf(List<OUTDTO> data, int partNumber, int totalParts, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generatePdf",
                "dataSize: " + data.size() + ", partNumber: " + partNumber + ", totalParts: " + totalParts + ", fields: " + fieldsToExport.size());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);

            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph(fileName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add pagination info if multiple parts
            if (totalParts > 1) {
                Font infoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                Paragraph info = new Paragraph("Part " + partNumber + " of " + totalParts, infoFont);
                info.setAlignment(Element.ALIGN_CENTER);
                document.add(info);
            }

            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(fieldsToExport.size());
            table.setWidthPercentage(100);

            // Add headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            for (FieldDetails field : fieldsToExport) {
                PdfPCell cell = new PdfPCell(new Phrase(field.getLabel(), headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add data
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (OUTDTO entity : data) {
                for (FieldDetails field : fieldsToExport) {
                    String value = getFieldValue(entity, field );
                    PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            byte[] result = outputStream.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "generatePdf", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generatePdf", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "PDF_GENERATED",
                    "Generated PDF with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;
        } catch (DocumentException e) {
            OSMLogger.logException(this.getClass(), "Error creating PDF document", e);
            throw new RuntimeException("Failed to create PDF", e);
        }
    }

    /**
     * Export data to CSV format
     *
     * @param exportDetails search criteria
     * @return CSV content as byte array (zipped if multiple files)
     */
//    public byte[] exportToCsv(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToCsv", exportDetails);
//
//        try {
//            if(exportDetails.getSearchData().isFilterTenant()) {
//                SearchDetails details = new SearchDetails();
//                details.setEqualValue(TenantContext.getCurrentTenant());
//                if(exportDetails.getSearchData().getSearchData() != null) {
//                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId",details);
//                }
//            }
//            // Get total count first to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // If total records exceed maximum per document, create multiple CSVs
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultipleCsvs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(), exportDetails.getFileName());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple CSVs for {} records", totalRecords);
//            } else {
//                result = createSingleCsv(exportDetails.getSearchData(), exportDetails.getFieldDetails());
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single CSV for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToCsv", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToCsv", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED",
//                    "CSV generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating CSV export", e);
//            throw e;
//        }
//    }
    public byte[] exportToCsv(ExportDetails exportDetails) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "exportToCsv", exportDetails);

        try {
            this.currentExportDetails = exportDetails; // Store for dynamic field processing

            if(exportDetails.getSearchData().isFilterTenant()) {
                SearchDetails details = new SearchDetails();
                details.setEqualValue(TenantContext.getCurrentTenant());
                if(exportDetails.getSearchData().getSearchData() != null) {
                    exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
                }
            }

            // Get total count first to determine if pagination is needed
            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
            SearchResponse<E, OUTDTO> countResponse = search(countData);
            long totalRecords = countResponse.getTotal();

            // Get sample data to process dynamic columns
            SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
            sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
            SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

            // Process collection fields and generate complete field list
            List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
            exportDetails.setFieldDetails(allFields);

            byte[] result;
            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
                result = createMultipleCsvs(exportDetails.getSearchData(), totalRecords, allFields, exportDetails.getFileName());
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple CSVs for {} records", totalRecords);
            } else {
                result = createSingleCsv(exportDetails.getSearchData(), allFields);
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single CSV for {} records", totalRecords);
            }

            OSMLogger.logMethodExit(this.getClass(), "exportToCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "exportToCsv", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED",
                    "CSV generated for " + totalRecords + " records (" + result.length + " bytes)");

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error generating CSV export", e);
            throw e;
        } finally {
            this.currentExportDetails = null; // Clear after use
        }
    }
    /**
     * Create a single CSV document
     *
     * @param searchData     search criteria
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] createSingleCsv(SearchData searchData, List<FieldDetails> fieldsToExport) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSingleCsv", "fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.logDataAccess(this.getClass(), "CSV_DATA_RETRIEVED", entityClass.getSimpleName());

            byte[] result = generateCsv(data, fieldsToExport);

            OSMLogger.logMethodExit(this.getClass(), "createSingleCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSingleCsv", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single CSV", e);
            throw e;
        }
    }

    /**
     * Create multiple CSV documents in a ZIP archive
     *
     * @param searchData     search criteria
     * @param totalRecords   total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultipleCsvs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultipleCsvs",
                "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_CSV_START",
                    "Creating " + totalPages + " CSV files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.logDataAccess(this.getClass(), "CSV_PAGE_DATA_RETRIEVED",
                            entityClass.getSimpleName());

                    // Generate CSV for current page
                    byte[] csvData = generateCsv(data, fieldsToExport);

                    // Add CSV to ZIP archive
                    String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                    ZipEntry entry = new ZipEntry(safeFileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".csv");
                    zipStream.putNextEntry(entry);
                    zipStream.write(csvData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "CSV_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "CSV_PAGE_COMPLETED",
                            "Completed CSV page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultipleCsvs", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultipleCsvs", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_CSV_COMPLETED",
                    "Successfully created " + totalPages + " CSV files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for CSVs", e);
            throw new RuntimeException("Failed to create CSV export", e);
        }
    }

    /**
     * Generate a CSV document for the given data
     *
     * @param data           list of entities
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] generateCsv(List<OUTDTO> data, List<FieldDetails> fieldsToExport) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateCsv",
                "dataSize: " + data.size() + ", fields: " + fieldsToExport.size());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // First, add UTF-8 BOM to help Excel detect encoding correctly
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            outputStream.write(bom);

            // Create Excel-friendly CSV with semicolon delimiter since some locales use comma as decimal separator
            StringBuilder csv = new StringBuilder();

            // Add headers
            for (int i = 0; i < fieldsToExport.size(); i++) {
                // Enclose header in quotes and make it stand out
                csv.append("\"").append(fieldsToExport.get(i).getLabel()).append("\"");
                if (i < fieldsToExport.size() - 1) {
                    csv.append(";");
                }
            }
            csv.append("\r\n"); // Windows-style line endings for better Excel compatibility

            // Add data rows
            for (OUTDTO entity : data) {
                for (int i = 0; i < fieldsToExport.size(); i++) {
                    String value = getFieldValue(entity, fieldsToExport.get(i));
                    // Properly escape quotes and wrap values in quotes
                    value = value.replace("\"", "\"\"");
                    csv.append("\"").append(value).append("\"");
                    if (i < fieldsToExport.size() - 1) {
                        csv.append(";");
                    }
                }
                csv.append("\r\n");
            }

            // Write the CSV content to the output stream
            outputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            byte[] result = outputStream.toByteArray();

            OSMLogger.logMethodExit(this.getClass(), "generateCsv", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generateCsv", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "CSV_GENERATED",
                    "Generated CSV with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating CSV document", e);
            throw new RuntimeException("Failed to create CSV", e);
        }
    }



    /**
     * Clone search data for count query
     *
     * @param original original search data
     * @return cloned search data with page=0 and size=1
     */
    private SearchData cloneSearchDataForCount(SearchData original) {
        SearchData clone = new SearchData();
        clone.setPage(0);
        clone.setSize(Integer.MAX_VALUE);
        clone.setSort(original.getSort());
        clone.setOrder(original.getOrder());
        clone.setSearchData(original.getSearchData());
        return clone;
    }


    /**
     * Get value of a field from an entity
     *
     * @param entity    entity object
     * @param fieldDetails fieldDetails
     * @return field value as string
     */
//    protected String getFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
//        if (entity == null || fieldDetails == null || fieldDetails.getName() == null || fieldDetails.getName().isEmpty()) {
//            return "";
//        }
//
//        // Handle nested properties by splitting the field name by dots
//        String[] fieldPath = fieldDetails.getName().split("\\.");
//
//        try {
//            Object currentObject = entity;
//            Class<?> currentClass = outDTOClass;
//
//            // Traverse the object hierarchy
//            for (String currentField : fieldPath) {
//                if (currentObject == null) {
//                    return "";
//                }
//
//                // Get the field from the current class
//                Field field = getFieldFromClass(currentClass, currentField);
//                if (field == null) {
//                    return "";
//                }
//
//                field.setAccessible(true);
//                currentObject = field.get(currentObject);
//
//                // Update the class for the next iteration if we have more fields to traverse
//                if (currentObject != null) {
//                    currentClass = currentObject.getClass();
//                }
//            }
//            if (currentObject != null) {
//                if (fieldDetails.isEnumValue() &&
//                        fieldDetails.getEnumValues() != null &&
//                        !fieldDetails.getEnumValues().isEmpty() && fieldDetails.getEnumValues().get(currentObject.toString())!=null) {
//                    return fieldDetails.getEnumValues().get(currentObject.toString());
//                }
//                return switch (currentObject) {
//                    case LocalDate localDate -> localDate.format(DATE_TIME_FORMATTER);
//                    case LocalDateTime localDateTime -> localDateTime.format(TIME_FORMATTER);
//                    case OffsetDateTime offsetDateTime -> offsetDateTime.format(OFFSET_DATE_TIME_FORMATTER1);
//                    case ZonedDateTime zonedDateTime -> zonedDateTime.format(ZONED_DATE_TIME_FORMATTER1);
//                    case Instant instant -> TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(instant);
//                    default -> currentObject.toString();
//                };
//            }
//            return "";
//        } catch (IllegalAccessException e) {
//            // Log the error if needed
//            LOGGER.error("Error accessing field " + fieldDetails.getName(), e);
//            return "";
//        }
//    }
    /**
     * Helper method to get a field from a class or its superclasses
     */
    private Field getFieldFromClass(Class<?> cls, String fieldName) {
        Class<?> currentClass = cls;

        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field not found in current class, check the superclass
                currentClass = currentClass.getSuperclass();
            }
        }

        return null; // Field not found in class hierarchy
    }


//    public byte[] exportToExcel(ExportDetails exportDetails) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "exportToExcel", exportDetails);
//
//        try {
//            // Get total count to determine if pagination is needed
//            SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
//            SearchResponse<E, OUTDTO> countResponse = search(countData);
//            long totalRecords = countResponse.getTotal();
//
//            byte[] result;
//            // Only create multiple Excel files if total records exceed maximum per document
//            if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
//                result = createMultipleExcelFiles(
//                        exportDetails.getSearchData(),
//                        totalRecords,
//                        exportDetails.getFieldDetails(),
//                        exportDetails.getFileName()
//                );
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple Excel files for {} records", totalRecords);
//            } else {
//                result = createSingleExcelFile(
//                        exportDetails.getSearchData(),
//                        exportDetails.getFieldDetails(),
//                        exportDetails.getFileName()
//                );
//                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single Excel file for {} records", totalRecords);
//            }
//
//            OSMLogger.logMethodExit(this.getClass(), "exportToExcel", "Generated " + result.length + " bytes");
//            OSMLogger.logPerformance(this.getClass(), "exportToExcel", startTime, System.currentTimeMillis());
//            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED",
//                    "Excel file generated for " + totalRecords + " records (" + result.length + " bytes)");
//
//            return result;
//        } catch (Exception e) {
//            OSMLogger.logException(this.getClass(), "Error generating Excel export", e);
//            throw e;
//        }
//    }
public byte[] exportToExcel(ExportDetails exportDetails) {
    long startTime = System.currentTimeMillis();
    OSMLogger.logMethodEntry(this.getClass(), "exportToExcel", exportDetails);

    try {
        this.currentExportDetails = exportDetails; // Store for dynamic field processing

        if(exportDetails.getSearchData().isFilterTenant()) {
            SearchDetails details = new SearchDetails();
            details.setEqualValue(TenantContext.getCurrentTenant());
            if(exportDetails.getSearchData().getSearchData() != null) {
                exportDetails.getSearchData().getSearchData().getSearch().put("tenantId", details);
            }
        }

        // Get total count to determine if pagination is needed
        SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
        SearchResponse<E, OUTDTO> countResponse = search(countData);
        long totalRecords = countResponse.getTotal();

        // Get sample data to process dynamic columns
        SearchData sampleData = cloneSearchDataForCount(exportDetails.getSearchData());
        sampleData.setSize(Math.min(100, (int) totalRecords)); // Sample first 100 records
        SearchResponse<E, OUTDTO> sampleResponse = search(sampleData);

        // Process collection fields and generate complete field list
        List<FieldDetails> allFields = processCollectionFields(exportDetails, sampleResponse.getData());
        exportDetails.setFieldDetails(allFields);

        byte[] result;
        if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
            result = createMultipleExcelFiles(
                    exportDetails.getSearchData(),
                    totalRecords,
                    allFields,
                    exportDetails.getFileName()
            );
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created multiple Excel files for {} records", totalRecords);
        } else {
            result = createSingleExcelFile(
                    exportDetails.getSearchData(),
                    allFields,
                    exportDetails.getFileName()
            );
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "Created single Excel file for {} records", totalRecords);
        }

        OSMLogger.logMethodExit(this.getClass(), "exportToExcel", "Generated " + result.length + " bytes");
        OSMLogger.logPerformance(this.getClass(), "exportToExcel", startTime, System.currentTimeMillis());
        OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED",
                "Excel file generated for " + totalRecords + " records (" + result.length + " bytes)");

        return result;
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error generating Excel export", e);
        throw e;
    } finally {
        this.currentExportDetails = null; // Clear after use
    }
}
    private byte[] createSingleExcelFile(SearchData searchData, List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSingleExcelFile", "fileName: " + fileName + ", fields: " + fieldsToExport.size());

        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E, OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Retrieved {} records for Excel export", data.size());
            OSMLogger.logDataAccess(this.getClass(), "EXCEL_DATA_RETRIEVED", entityClass.getSimpleName());

            // Generate single Excel file
            byte[] result = generateExcelFile(data, fieldsToExport, fileName);

            OSMLogger.logMethodExit(this.getClass(), "createSingleExcelFile", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createSingleExcelFile", startTime, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating single Excel file", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private byte[] createMultipleExcelFiles(SearchData searchData, long totalRecords,
                                            List<FieldDetails> fieldsToExport, String fileName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createMultipleExcelFiles",
                "totalRecords: " + totalRecords + ", fields: " + fieldsToExport.size() + ", fileName: " + fileName);

        try {
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Creating {} Excel files for {} total records", totalPages, totalRecords);
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_EXCEL_START",
                    "Creating " + totalPages + " Excel files for " + totalRecords + " records");

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    long pageStartTime = System.currentTimeMillis();

                    // Update search criteria for current page
                    searchData.setPage(pageNum);
                    searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                    // Get data for current page
                    SearchResponse<E, OUTDTO> response = search(searchData);
                    List<OUTDTO> data = response.getData();

                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Processing page {} with {} records", pageNum + 1, data.size());
                    OSMLogger.logDataAccess(this.getClass(), "EXCEL_PAGE_DATA_RETRIEVED",
                            entityClass.getSimpleName());

                    // Generate Excel for current page
                    String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                    String pageSuffix = "_part_" + (pageNum + 1) + "_of_" + totalPages;
                    byte[] excelData = generateExcelFile(data, fieldsToExport, safeFileName + pageSuffix);

                    // Add Excel to ZIP archive
                    ZipEntry entry = new ZipEntry(safeFileName + pageSuffix + ".xlsx");
                    zipStream.putNextEntry(entry);
                    zipStream.write(excelData);
                    zipStream.closeEntry();

                    OSMLogger.logPerformance(this.getClass(), "EXCEL_PAGE_GENERATION", pageStartTime, System.currentTimeMillis());
                    OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_PAGE_COMPLETED",
                            "Completed Excel page " + (pageNum + 1) + " of " + totalPages);
                }
            }

            byte[] result = zipOutput.toByteArray();
            OSMLogger.logMethodExit(this.getClass(), "createMultipleExcelFiles", "Generated ZIP with " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "createMultipleExcelFiles", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "MULTIPLE_EXCEL_COMPLETED",
                    "Successfully created " + totalPages + " Excel files in ZIP archive");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating ZIP archive for Excel files", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private byte[] generateExcelFile(List<OUTDTO> data, List<FieldDetails> fieldsToExport, String sheetName) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateExcelFile",
                "dataSize: " + data.size() + ", fields: " + fieldsToExport.size() + ", sheetName: " + sheetName);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Validate input
            if (data == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "No data provided for Excel export");
                data = Collections.emptyList();
            }

            if (fieldsToExport == null || fieldsToExport.isEmpty()) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "No fields specified for Excel export");
                fieldsToExport = Collections.emptyList();
            }

            // Create sheet with safe name (Excel has 31 char limit and prohibits certain chars)
            String safeName = sheetName != null ? sheetName : "Data Export";
            safeName = safeName.replaceAll("[\\\\/:*?\"<>|\\[\\]]", "_");
            if (safeName.length() > 31) {
                safeName = safeName.substring(0, 31);
            }
            XSSFSheet sheet = workbook.createSheet(safeName);

            // Create header row and style
            XSSFRow headerRow = sheet.createRow(0);
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Add headers
            for (int i = 0; i < fieldsToExport.size(); i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(fieldsToExport.get(i).getLabel());
                cell.setCellStyle(headerStyle);
            }

            // Create data styles
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle altRowStyle = workbook.createCellStyle();
            altRowStyle.cloneStyleFrom(dataStyle);
            altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Add data rows
            for (int rowNum = 0; rowNum < data.size(); rowNum++) {
                XSSFRow row = sheet.createRow(rowNum + 1); // +1 because header is at 0
                OUTDTO entity = data.get(rowNum);

                // Select style based on row number (striped rows)
                XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? dataStyle : altRowStyle;

                for (int colNum = 0; colNum < fieldsToExport.size(); colNum++) {
                    XSSFCell cell = row.createCell(colNum);
                    String value = getFieldValue(entity, fieldsToExport.get(colNum) );
                    cell.setCellValue(value);
                    cell.setCellStyle(rowStyle);
                }
            }

            // Auto-size columns for better readability
            for (int i = 0; i < fieldsToExport.size(); i++) {
                sheet.autoSizeColumn(i);
                // Cap column width at 50 characters to prevent excessive width
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Freeze the header row
            sheet.createFreezePane(0, 1);

            // Write the workbook to the output stream
            workbook.write(outputStream);
            byte[] result = outputStream.toByteArray();

            OSMLogger.logMethodExit(this.getClass(), "generateExcelFile", "Generated " + result.length + " bytes");
            OSMLogger.logPerformance(this.getClass(), "generateExcelFile", startTime, System.currentTimeMillis());
            OSMLogger.logBusinessEvent(this.getClass(), "EXCEL_GENERATED",
                    "Generated Excel file with " + data.size() + " records, " + fieldsToExport.size() + " fields");

            return result;

        } catch (IOException e) {
            OSMLogger.logException(this.getClass(), "Error creating Excel document", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private List<FieldDetails> processCollectionFields(ExportDetails exportDetails, List<OUTDTO> sampleData) {
        List<FieldDetails> allFields = new ArrayList<>(exportDetails.getFieldDetails());

        if (exportDetails.getCollectionFields() == null || exportDetails.getCollectionFields().isEmpty()) {
            return allFields;
        }

        // Use a sample of data to determine dynamic columns
        Set<String> dynamicColumns = new HashSet<>();

        for (CollectionFieldDetails collectionField : exportDetails.getCollectionFields()) {
            Set<String> columnNames = extractColumnNamesFromCollection(sampleData, collectionField);

            for (String columnName : columnNames) {
                String prefixedName = collectionField.getColumnPrefix() != null
                        ? collectionField.getColumnPrefix() + columnName
                        : columnName;

                if (!dynamicColumns.contains(prefixedName)) {
                    FieldDetails dynamicField = new FieldDetails();
                    dynamicField.setName(collectionField.getCollectionPath() + "." + columnName);
                    dynamicField.setLabel(prefixedName);
                    dynamicField.setDynamicColumn(true);
                    dynamicField.setSourceCollection(collectionField.getCollectionPath());

                    allFields.add(dynamicField);
                    dynamicColumns.add(prefixedName);
                }
            }
        }

        return allFields;
    }
    private Set<String> extractColumnNamesFromCollection(List<OUTDTO> data, CollectionFieldDetails collectionField) {
        Set<String> columnNames = new HashSet<>();

        for (OUTDTO entity : data) {
            try {
                Object collection = getNestedFieldValue(entity, collectionField.getCollectionPath());
                if (collection instanceof Collection) {
                    for (Object item : (Collection<?>) collection) {
                        String columnName = getNestedFieldValueAsString(item, collectionField.getNameField());
                        if (columnName != null && !columnName.isEmpty()) {
                            columnNames.add(columnName);
                        }
                    }
                }
            } catch (Exception e) {
                OSMLogger.logException(this.getClass(), "Error extracting column names from collection", e);
            }
        }

        return columnNames;
    }
    private Object getNestedFieldValue(Object entity, String fieldPath) throws IllegalAccessException {
        if (entity == null || fieldPath == null || fieldPath.isEmpty()) {
            return null;
        }

        String[] pathParts = fieldPath.split("\\.");
        Object currentObject = entity;
        Class<?> currentClass = entity.getClass();

        for (String fieldName : pathParts) {
            if (currentObject == null) {
                return null;
            }

            Field field = getFieldFromClass(currentClass, fieldName);
            if (field == null) {
                return null;
            }

            field.setAccessible(true);
            currentObject = field.get(currentObject);

            if (currentObject != null) {
                currentClass = currentObject.getClass();
            }
        }

        return currentObject;
    }
    private String getNestedFieldValueAsString(Object entity, String fieldPath) {
        try {
            Object value = getNestedFieldValue(entity, fieldPath);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    private String getDynamicCollectionFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
        try {
            // Find the collection field configuration
            CollectionFieldDetails collectionConfig = findCollectionConfig(fieldDetails.getSourceCollection());
            if (collectionConfig == null) {
                return "";
            }

            // Get the collection
            Object collection = getNestedFieldValue(entity, collectionConfig.getCollectionPath());
            if (!(collection instanceof Collection)) {
                return "";
            }

            // Extract the column name from the field details
            String targetColumnName = fieldDetails.getLabel();
            if (collectionConfig.getColumnPrefix() != null) {
                targetColumnName = targetColumnName.replace(collectionConfig.getColumnPrefix(), "");
            }

            // Find the matching item in the collection
            for (Object item : (Collection<?>) collection) {
                String itemColumnName = getNestedFieldValueAsString(item, collectionConfig.getNameField());
                if (targetColumnName.equals(itemColumnName)) {
                    return getNestedFieldValueAsString(item, collectionConfig.getValueField());
                }
            }

            return "";
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting dynamic collection field value", e);
            return "";
        }
    }
    protected String getFieldValue(OUTDTO entity, FieldDetails fieldDetails) {
        if (entity == null || fieldDetails == null || fieldDetails.getName() == null || fieldDetails.getName().isEmpty()) {
            return "";
        }

        // Handle dynamic columns from collections
        if (fieldDetails.isDynamicColumn() && fieldDetails.getSourceCollection() != null) {
            return getDynamicCollectionFieldValue(entity, fieldDetails);
        }

        // Handle regular fields (existing logic)
        String[] fieldPath = fieldDetails.getName().split("\\.");

        try {
            Object currentObject = entity;
            Class<?> currentClass = outDTOClass;

            for (String currentField : fieldPath) {
                if (currentObject == null) {
                    return "";
                }

                Field field = getFieldFromClass(currentClass, currentField);
                if (field == null) {
                    return "";
                }

                field.setAccessible(true);
                currentObject = field.get(currentObject);

                if (currentObject != null) {
                    currentClass = currentObject.getClass();
                }
            }

            if (currentObject != null) {
                if (fieldDetails.isEnumValue() &&
                        fieldDetails.getEnumValues() != null &&
                        !fieldDetails.getEnumValues().isEmpty() &&
                        fieldDetails.getEnumValues().get(currentObject.toString()) != null) {
                    return fieldDetails.getEnumValues().get(currentObject.toString());
                }
                return formatFieldValue(currentObject);
            }
            return "";
        } catch (IllegalAccessException e) {
            LOGGER.error("Error accessing field " + fieldDetails.getName(), e);
            return "";
        }
    }
    private CollectionFieldDetails findCollectionConfig(String collectionPath) {
        if (currentExportDetails != null && currentExportDetails.getCollectionFields() != null) {
            return currentExportDetails.getCollectionFields().stream()
                    .filter(config -> collectionPath.equals(config.getCollectionPath()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    private String formatFieldValue(Object value) {
        return switch (value) {
            case LocalDate localDate -> localDate.format(DATE_TIME_FORMATTER);
            case LocalDateTime localDateTime -> localDateTime.format(TIME_FORMATTER);
            case OffsetDateTime offsetDateTime -> offsetDateTime.format(OFFSET_DATE_TIME_FORMATTER1);
            case ZonedDateTime zonedDateTime -> zonedDateTime.format(ZONED_DATE_TIME_FORMATTER1);
            case Instant instant -> TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(instant);
            default -> value.toString();
        };
    }
}
