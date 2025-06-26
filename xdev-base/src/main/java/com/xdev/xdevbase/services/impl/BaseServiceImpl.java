package com.xdev.xdevbase.services.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.xdevbase.apiDTOs.SearchResponse;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.ExportDetails;
import com.xdev.xdevbase.models.FieldDetails;
import com.xdev.xdevbase.models.SearchData;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.services.utils.SearchSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unchecked")
public abstract class BaseServiceImpl<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> implements BaseService<E, INDTO, OUTDTO> {
    private static final int MAX_RECORDS_PER_DOCUMENT = 1000;
    protected final BaseRepository<E> repository;
    protected final ModelMapper modelMapper;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<Object, Object> lMap = new ConcurrentHashMap<>();
    protected Class<E> entityClass;
    protected Class<INDTO> inDTOClass;
    protected Class<OUTDTO> outDTOClass;
    @Autowired
    private SearchSpecificationBuilder<E> specificationBuilder;

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
        LOGGER.debug("Find By Id Start");
        Optional<E> data = repository.findById(id);
        if (data.isEmpty()) throw new EntityNotFoundException("Entity not found with this id " + id);
        else {
            LOGGER.debug("Find By Id End");
            return modelMapper.map(data.get(), outDTOClass);
        }
    }

    @Override
    public List<OUTDTO> findAll() {
        LOGGER.debug("Find all Start");
        Collection<E> data = repository.findAll();
        LOGGER.debug("Find all End");
        return data.stream().map(item -> modelMapper.map(item, outDTOClass)).toList();
    }

    @Override
    public Page<OUTDTO> findAll(int page, int size, String sort, String direction) {
        LOGGER.debug("Find all pageable start");
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);  // "ASC" or "DESC"
        Sort sortObject = Sort.by(sortDirection, sort);  // Sort by the field and direction
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<E> data = repository.findAll(pageable);

        LOGGER.debug("Find all pageable end");
        return data.map(item -> modelMapper.map(item, outDTOClass));

    }
   @Override
    public OUTDTO save(INDTO request) {
        if (request == null) {
            this.LOGGER.debug("Request is empty or null");
            return null;
        } else {
            this.LOGGER.debug("save entity start");
            E entity = (E) this.modelMapper.map(request, this.entityClass);

            resolveEntityRelations(entity); // ✅ Fix here

            E savedEntity = (E) this.repository.save(entity);
            this.LOGGER.debug("save entity end");
            return (OUTDTO) this.modelMapper.map(savedEntity, this.outDTOClass);
        }
    }
   @Override
    public List<OUTDTO> save(List<INDTO> request) {
        if (request != null && !request.isEmpty()) {
            this.LOGGER.debug("save entities start");
            List<E> entities = request.stream()
                    .map((item) -> (E) this.modelMapper.map(item, this.entityClass))
                    .toList();

            entities.forEach(this::resolveEntityRelations); // ✅ Fix here

            entities = this.repository.saveAll(entities);
            this.LOGGER.debug("save entities end");
            return entities.stream()
                    .map((item) -> (OUTDTO) this.modelMapper.map(item, this.outDTOClass))
                    .toList();
        } else {
            this.LOGGER.debug("Request list is empty or null");
            return Collections.emptyList();
        }
    }
  @Override
    public OUTDTO update(INDTO request) {
        if (request != null && request.getId() != null) {
            Optional<E> existedOptEntity = this.repository.findById(request.getId());
            if (existedOptEntity.isEmpty()) {
                this.LOGGER.debug("Entity with ID {} not found for update", request.getId());
                return null;
            } else {
                E existedEntity = existedOptEntity.get();
                this.modelMapper.map(request, existedEntity);

                resolveEntityRelations(existedEntity); // ✅ Fix here

                E updatedEntity = (E) this.repository.save(existedEntity);
                this.LOGGER.debug("Entity with ID {} updated successfully", request.getId());
                return (OUTDTO) this.modelMapper.map(updatedEntity, this.outDTOClass);
            }
        } else {
            this.LOGGER.debug("Request or ID is null: {}", request);
            return null;
        }
    }

   @Override
   public void resolveEntityRelations(E entity) {
    }


    @Override
   public void remove(UUID id) {
        if (id != null) {
            LOGGER.debug("deleting entity start");
            repository.deleteById(id);
            LOGGER.debug("deleting entity end");
        } else {
            LOGGER.debug("Entity is null");
        }

    }

    @Override
    public OUTDTO delete(UUID id) {
        if (id == null) {
            LOGGER.debug(" ID is null: {}",id);
            return null;
        }
        E entity = repository.findById(id).orElse(null);
        if (entity == null) {
            LOGGER.debug("No such entity with id = {}", id);
            return null;
        }
        LOGGER.debug("Logical delete start");
        entity.setDeleted(true);
        E updatedEntity = repository.save(entity);
        LOGGER.debug("Logical delete end");
        return modelMapper.map(updatedEntity, outDTOClass);
    }

    @Override
    public void removeAll(Collection<INDTO> entities) {
        if (entities != null) {
            LOGGER.debug("Deleting entities start");
            List<E> entitiesToDelete = entities.stream().map(item -> modelMapper.map(item, entityClass)).toList();
            repository.deleteAll(entitiesToDelete);
            LOGGER.debug("Deleting entities end");
        } else {
            LOGGER.debug("Entities to delete are null");
        }
    }


    @Override
    public Optional<Revision<Integer, E>> findLastRevisionById(UUID id) {
        LOGGER.debug("findLastRevisionById Start");
        Optional<Revision<Integer, E>> data = this.repository.findLastChangeRevision(id);
        LOGGER.debug("findLastRevisionById End");
        return data;
    }

    @Override
    public Revisions<Integer, E> findRevisionsById(UUID id) {
        LOGGER.debug("findRevisions Start");
        Revisions<Integer, E> data = this.repository.findRevisions(id);
        LOGGER.debug("findRevisions End");
        return data;
    }

    @Override
    public SearchResponse<E,OUTDTO> search(SearchData searchData) {
        try {
            int page = searchData.getPage() != null ? searchData.getPage() : 0;
            int size = searchData.getSize() != null ? searchData.getSize() : 10;
            Sort.Direction direction =(searchData.getOrder()!=null && searchData.getOrder().equalsIgnoreCase("DESC")) ? Sort.Direction.DESC : Sort.Direction.ASC;
            String sort = searchData.getSort()!=null ? searchData.getSort() : "createdDate";
            Pageable pageable=PageRequest.of(page, size, direction, sort);

            Specification<E> spec = null;
            if (searchData.getSearchData() != null) {
                spec = specificationBuilder.buildSpecification(searchData.getSearchData());
            }

            Page<E> result;
            if (spec != null) {
                result = repository.findAll(spec, pageable);
            } else {
                result = repository.findAll(pageable);
            }
            List<OUTDTO> dtos = result.getContent().stream().map(
                    element -> modelMapper.map(element, outDTOClass)
            ).toList();
            return new SearchResponse<>(
                    result.getTotalElements(),
                    dtos,
                    result.getTotalPages(),
                    result.getNumber() + 1
            );
        }catch (Exception e) {
            return new SearchResponse<>(
                    0,
                    null,
                    0,
                    0
            );
        }
    }





    public byte[] exportToPdf(ExportDetails exportDetails) {
        // Get total count first to determine if pagination is needed
        SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
        SearchResponse<E,OUTDTO> countResponse = search(countData);
        long totalRecords = countResponse.getTotal();


        // If total records exceed maximum per document, create multiple PDFs
        if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
            return createMultiplePdfs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(),exportDetails.getFileName());
        } else {
            return createSinglePdf(exportDetails.getSearchData(), exportDetails.getFieldDetails(),exportDetails.getFileName());
        }
    }

    /**
     * Create a single PDF document
     * @param searchData search criteria
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[] createSinglePdf(SearchData searchData, List<FieldDetails> fieldsToExport,String fileName) {
        // Retrieve all data for export
        searchData.setPage(0);
        searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
        SearchResponse<E,OUTDTO> response = search(searchData);
        List<OUTDTO> data = response.getData();

        return generatePdf(data, 1, 1, fieldsToExport, fileName);
    }

    /**
     * Create multiple PDF documents in a ZIP archive
     * @param searchData search criteria
     * @param totalRecords total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultiplePdfs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport,String fileName) {
        ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();

        try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                // Update search criteria for current page
                searchData.setPage(pageNum);
                searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                // Get data for current page
                SearchResponse<E, OUTDTO> response = search(searchData);
                List<OUTDTO> data = response.getData();

                // Generate PDF for current page
                byte[] pdfData = generatePdf(data, pageNum + 1, totalPages, fieldsToExport,fileName);

                // Add PDF to ZIP archive
                ZipEntry entry = new ZipEntry(fileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".pdf");
                zipStream.putNextEntry(entry);
                zipStream.write(pdfData);
                zipStream.closeEntry();
            }

        } catch (IOException e) {
            LOGGER.error("Error creating ZIP archive for PDFs", e);
            throw new RuntimeException("Failed to create PDF export", e);

        }
        return zipOutput.toByteArray();

    }

    /**
     * Generate a PDF document for the given data
     * @param data list of entities
     * @param partNumber current part number
     * @param totalParts total number of parts
     * @param fieldsToExport fields to include in export
     * @return PDF content as byte array
     */
    private byte[]
    generatePdf(List<OUTDTO> data, int partNumber, int totalParts, List<FieldDetails> fieldsToExport,String fileName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph(fileName , titleFont);
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
                    String value = getFieldValue(entity, field.getName());
                    PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException e) {
            LOGGER.error("Error creating PDF document", e);
            throw new RuntimeException("Failed to create PDF", e);
        }
    }

    /**
     * Export data to CSV format
     * @param exportDetails search criteria
     * @return CSV content as byte array (zipped if multiple files)
     */
    public byte[] exportToCsv(ExportDetails exportDetails) {
        // Get total count first to determine if pagination is needed
        SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
        SearchResponse<E,OUTDTO> countResponse = search(countData);
        long totalRecords = countResponse.getTotal();

        // If total records exceed maximum per document, create multiple CSVs
        if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
            return createMultipleCsvs(exportDetails.getSearchData(), totalRecords, exportDetails.getFieldDetails(), exportDetails.getFileName());
        } else {
            return createSingleCsv(exportDetails.getSearchData(), exportDetails.getFieldDetails());
        }
    }

    /**
     * Create a single CSV document
     * @param searchData search criteria
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] createSingleCsv(SearchData searchData, List<FieldDetails> fieldsToExport) {
        // Retrieve all data for export
        searchData.setPage(0);
        searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
        SearchResponse<E,OUTDTO> response = search(searchData);
        List<OUTDTO> data = response.getData();

        return generateCsv(data, fieldsToExport);
    }

    /**
     * Create multiple CSV documents in a ZIP archive
     * @param searchData search criteria
     * @param totalRecords total number of records
     * @param fieldsToExport fields to include in export
     * @return ZIP archive as byte array
     */
    private byte[] createMultipleCsvs(SearchData searchData, long totalRecords, List<FieldDetails> fieldsToExport, String fileName) {
        ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();

        try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);

            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                // Update search criteria for current page
                searchData.setPage(pageNum);
                searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                // Get data for current page
                SearchResponse<E,OUTDTO> response = search(searchData);
                List<OUTDTO> data = response.getData();

                // Generate CSV for current page
                byte[] csvData = generateCsv(data, fieldsToExport);

                // Add CSV to ZIP archive
                String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                ZipEntry entry = new ZipEntry(safeFileName + "_part_" + (pageNum + 1) + "_of_" + totalPages + ".csv");
                zipStream.putNextEntry(entry);
                zipStream.write(csvData);
                zipStream.closeEntry();
            }

        } catch (IOException e) {
            LOGGER.error("Error creating ZIP archive for CSVs", e);
            throw new RuntimeException("Failed to create CSV export", e);
        }

        return zipOutput.toByteArray();
    }
    /**
     * Generate a CSV document for the given data
     * @param data list of entities
     * @param fieldsToExport fields to include in export
     * @return CSV content as byte array
     */
    private byte[] generateCsv(List<OUTDTO> data, List<FieldDetails> fieldsToExport) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // First, add UTF-8 BOM to help Excel detect encoding correctly
            byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
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
                    String value = getFieldValue(entity, fieldsToExport.get(i).getName());
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
            return outputStream.toByteArray();

        } catch (IOException e) {
            LOGGER.error("Error creating CSV document", e);
            throw new RuntimeException("Failed to create CSV", e);
        }
    }
    /**
     * Get all fields available in the entity
     * @return list of all field names
     */
    protected List<String> getAllEntityFields() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get default fields to export (can be overridden by subclasses)
     * @return list of default fields
     */
    protected List<String> getDefaultExportFields() {
        return getAllEntityFields();
    }

    /**
     * Clone search data for count query
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
     * @param entity entity object
     * @param fieldName field name
     * @return field value as string
     */
    protected String getFieldValue(OUTDTO entity, String fieldName) {
        if (entity == null || fieldName == null || fieldName.isEmpty()) {
            return "";
        }

        // Handle nested properties by splitting the field name by dots
        String[] fieldPath = fieldName.split("\\.");

        try {
            Object currentObject = entity;
            Class<?> currentClass = outDTOClass;

            // Traverse the object hierarchy
            for (String currentField : fieldPath) {
                if (currentObject == null) {
                    return "";
                }

                // Get the field from the current class
                Field field = getFieldFromClass(currentClass, currentField);
                if (field == null) {
                    return "";
                }

                field.setAccessible(true);
                currentObject =  field.get(currentObject);

                // Update the class for the next iteration if we have more fields to traverse
                if (currentObject != null) {
                    currentClass = currentObject.getClass();
                }
            }

            return currentObject != null ? currentObject.toString() : "";
        } catch (IllegalAccessException e) {
            // Log the error if needed
             LOGGER.error("Error accessing field " + fieldName, e);
            return "";
        }
    }

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














    public byte[] exportToExcel(ExportDetails exportDetails) {
        // Get total count to determine if pagination is needed
        SearchData countData = cloneSearchDataForCount(exportDetails.getSearchData());
        SearchResponse<E,OUTDTO> countResponse = search(countData);
        long totalRecords = countResponse.getTotal();

        LOGGER.debug("Total records for export: {}", totalRecords);

        // Only create multiple Excel files if total records exceed maximum per document
        if (totalRecords > MAX_RECORDS_PER_DOCUMENT) {
            LOGGER.debug("Creating multiple Excel files in ZIP archive");
            return createMultipleExcelFiles(
                    exportDetails.getSearchData(),
                    totalRecords,
                    exportDetails.getFieldDetails(),
                    exportDetails.getFileName()
            );
        } else {
            LOGGER.debug("Creating single Excel file");
            return createSingleExcelFile(
                    exportDetails.getSearchData(),
                    exportDetails.getFieldDetails(),
                    exportDetails.getFileName()
            );
        }
    }

    private byte[] createSingleExcelFile(SearchData searchData, List<FieldDetails> fieldsToExport, String fileName) {
        try {
            // Retrieve all data for export
            searchData.setPage(0);
            searchData.setSize(MAX_RECORDS_PER_DOCUMENT);
            SearchResponse<E,OUTDTO> response = search(searchData);
            List<OUTDTO> data = response.getData();

            LOGGER.debug("Retrieved {} records for Excel export", data.size());

            // Generate single Excel file
            return generateExcelFile(data, fieldsToExport, fileName);
        } catch (Exception e) {
            LOGGER.error("Error creating single Excel file", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }

    private byte[] createMultipleExcelFiles(SearchData searchData, long totalRecords,
                                            List<FieldDetails> fieldsToExport, String fileName) {
        ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();

        try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
            int totalPages = (int) Math.ceil((double) totalRecords / MAX_RECORDS_PER_DOCUMENT);
            LOGGER.debug("Creating {} Excel files for {} total records", totalPages, totalRecords);

            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                // Update search criteria for current page
                searchData.setPage(pageNum);
                searchData.setSize(MAX_RECORDS_PER_DOCUMENT);

                // Get data for current page
                SearchResponse<E,OUTDTO> response = search(searchData);
                List<OUTDTO> data = response.getData();
                LOGGER.debug("Processing page {} with {} records", pageNum + 1, data.size());

                // Generate Excel for current page
                String safeFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "file";
                String pageSuffix = "_part_" + (pageNum + 1) + "_of_" + totalPages;
                byte[] excelData = generateExcelFile(data, fieldsToExport, safeFileName + pageSuffix);

                // Add Excel to ZIP archive
                ZipEntry entry = new ZipEntry(safeFileName + pageSuffix + ".xlsx");
                zipStream.putNextEntry(entry);
                zipStream.write(excelData);
                zipStream.closeEntry();
            }

        } catch (IOException e) {
            LOGGER.error("Error creating ZIP archive for Excel files", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }

        return zipOutput.toByteArray();
    }

    private byte[] generateExcelFile(List<OUTDTO> data, List<FieldDetails> fieldsToExport, String sheetName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Validate input
            if (data == null) {
                LOGGER.warn("No data provided for Excel export");
                data = Collections.emptyList();
            }

            if (fieldsToExport == null || fieldsToExport.isEmpty()) {
                LOGGER.warn("No fields specified for Excel export");
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
                    String value = getFieldValue(entity, fieldsToExport.get(colNum).getName());
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
            return outputStream.toByteArray();

        } catch (IOException e) {
            LOGGER.error("Error creating Excel document", e);
            throw new RuntimeException("Failed to create Excel export", e);
        }
    }}
