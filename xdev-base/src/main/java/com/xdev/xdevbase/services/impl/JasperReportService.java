package com.xdev.xdevbase.services.impl;


import com.xdev.xdevbase.config.JasperReportConfig;
import com.xdev.xdevbase.config.exceptions.PdfGenerationException;
import com.xdev.xdevbase.config.exceptions.TemplateNotFoundException;
import com.xdev.xdevbase.config.exceptions.TemplateUploadException;
import com.xdev.xdevbase.dtos.reporting.PdfGenerationRequest;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import java.util.*;

@Service
public class JasperReportService {

    private final JasperReportConfig jasperConfig;

    public JasperReportService(JasperReportConfig jasperConfig) {
        this.jasperConfig = jasperConfig;
    }
    /**
     * Upload a Jasper template file (.jrxml or .jasper)
     */
    public String uploadTemplate(MultipartFile file, String customFileName) {
        if (file.isEmpty()) {
            throw new TemplateUploadException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new TemplateUploadException("Invalid file name");
        }

        String extension = FilenameUtils.getExtension(originalFileName);
        if (!extension.equals("jrxml") && !extension.equals("jasper")) {
            throw new TemplateUploadException("Only .jrxml and .jasper files are allowed");
        }

        try {
            String templatePath = jasperConfig.getTemplates().getPath();

            String fileName = customFileName + "." + extension;
            File destinationFile = new File(templatePath, fileName);

            file.transferTo(destinationFile);

            if (extension.equals("jrxml")) {
                compileTemplate(destinationFile.getAbsolutePath());
            }

            return destinationFile.getAbsolutePath();

        } catch (IOException e) {
            throw new TemplateUploadException("Failed to upload template", e);
        } catch (JRException e) {
            throw new TemplateUploadException("Failed to compile template", e);
        }
    }
    /**
     * Compile .jrxml to .jasper
     */
    private void compileTemplate(String jrxmlPath) throws JRException {
        String jasperPath = jrxmlPath.replace(".jrxml", ".jasper");
        JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
    }

    /**
     * Generate PDF from template
     */
    public byte[] generatePdf(PdfGenerationRequest request) {
        try {
            String templatePath = jasperConfig.getTemplates().getPath();
            String templateName = request.getTemplateName();

            if (!templateName.endsWith(".jasper")) {
                templateName = FilenameUtils.getBaseName(templateName) + ".jasper";
            }

            File templateFile = new File(templatePath, templateName);

            if (!templateFile.exists()) {
                throw new TemplateNotFoundException("Template not found: " + templateName);
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(templateFile);

            Map<String, Object> parameters = request.getParameters() != null
                    ? new HashMap<>(request.getParameters())
                    : new HashMap<>();
            if (request.getDataSource() != null && !request.getDataSource().isEmpty()) {
                JRDataSource dataSource = new JRBeanCollectionDataSource(request.getDataSource());
                parameters.put("dataSource", dataSource);
            }
            JRDataSource ds = new JREmptyDataSource();
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parameters,
                    ds
            );

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            return pdfBytes;

        } catch (JRException e) {
            throw new PdfGenerationException("Failed to generate PDF", e);
        }
    }

    /**
     * Generate PDF and save to file
     */
    public File generatePdfToFile(PdfGenerationRequest request) {
        try {
            byte[] pdfBytes = generatePdf(request);

            String outputPath = jasperConfig.getReports().getOutputPath();
            String fileName = request.getOutputFileName() != null
                    ? request.getOutputFileName()
                    : "report_" + System.currentTimeMillis() + ".pdf";

            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            File outputFile = new File(outputPath, fileName);
            FileUtils.writeByteArrayToFile(outputFile, pdfBytes);

            return outputFile;

        } catch (IOException e) {
            throw new PdfGenerationException("Failed to save PDF to file", e);
        }
    }


    /**
     * Download template file
     */
    public byte[] downloadTemplate(String templateName) {
        String templatePath = jasperConfig.getTemplates().getPath();
        File templateFile = new File(templatePath, templateName);

        if (!templateFile.exists()) {
            throw new TemplateNotFoundException("Template not found: " + templateName);
        }
        try {
            return FileUtils.readFileToByteArray(templateFile);
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to read template file", e);
        }
    }
}