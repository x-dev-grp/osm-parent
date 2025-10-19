package com.xdev.xdevbase.controllers.impl;

import com.xdev.xdevbase.dtos.reporting.PdfGenerationRequest;
import com.xdev.xdevbase.dtos.reporting.PdfGenerationResponse;
import com.xdev.xdevbase.dtos.reporting.TemplateUploadResponse;
import com.xdev.xdevbase.services.impl.JasperReportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/api/jasper")
@CrossOrigin(origins = "*")
public class JasperReportController {

    private final JasperReportService jasperReportService;
    public JasperReportController(JasperReportService jasperReportService) {
        this.jasperReportService = jasperReportService;
    }
    /**
     * Upload a Jasper template
     */
    @PostMapping("/templates/upload")
    public ResponseEntity<TemplateUploadResponse> uploadTemplate(
            @RequestParam("file") MultipartFile file,@RequestBody String templateType) {
        try {
            String templatePath = jasperReportService.uploadTemplate(file, templateType);
            TemplateUploadResponse response = new TemplateUploadResponse();
            response.setTemplatePath(templatePath);
            response.setFileName(file.getOriginalFilename());
            response.setMessage("Template uploaded successfully");
            response.setSuccess(true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all templates
     */
//    @GetMapping("/templates")
//    public ResponseEntity<List<TemplateInfo>> getAllTemplates() {
//        List<TemplateInfo> templates = jasperReportService.getAllTemplates();
//        return ResponseEntity.ok(templates);
//    }

    /**
     * Delete a template
     */
//    @DeleteMapping("/templates/{templateName}")
//    public ResponseEntity<TemplateUploadResponse> deleteTemplate(
//            @PathVariable String templateName) {
//
//        boolean deleted = jasperReportService.deleteTemplate(templateName);
//
//        TemplateUploadResponse response = TemplateUploadResponse.builder()
//                .fileName(templateName)
//                .message(deleted ? "Template deleted successfully" : "Failed to delete template")
//                .success(deleted)
//                .build();
//
//        return ResponseEntity.ok(response);
//    }

    /**
     * Download a template
     */
    @GetMapping("/templates/download/{templateName}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable String templateName) {

        byte[] templateBytes = jasperReportService.downloadTemplate(templateName);
        ByteArrayResource resource = new ByteArrayResource(templateBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(templateBytes.length)
                .body(resource);
    }

    /**
     * Generate PDF and return as download
     */
    @PostMapping("/generate")
    public ResponseEntity<Resource> generatePdf(@RequestBody PdfGenerationRequest request) {

        byte[] pdfBytes = jasperReportService.generatePdf(request);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        String fileName = request.getOutputFileName() != null
                ? request.getOutputFileName()
                : "report.pdf";

        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    /**
     * Generate PDF and save to server (returns file info)
     */
    @PostMapping("/generate/save")
    public ResponseEntity<PdfGenerationResponse> generateAndSavePdf(
            @RequestBody PdfGenerationRequest request) {

        File pdfFile = jasperReportService.generatePdfToFile(request);
        PdfGenerationResponse response = new PdfGenerationResponse();
        response.setFileName(pdfFile.getName());
        response.setMessage("PDF generated and saved successfully");
        response.setDownloadUrl("/api/jasper/download/" + pdfFile.getName());
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    /**
     * Preview PDF in browser (inline display)
     */
//    @PostMapping("/generate/preview")
//    public ResponseEntity<Resource> previewPdf(@RequestBody PdfGenerationRequest request) {
//
//        byte[] pdfBytes = jasperReportService.generatePdf(request);
//        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
//                .contentType(MediaType.APPLICATION_PDF)
//                .contentLength(pdfBytes.length)
//                .body(resource);
//    }
}