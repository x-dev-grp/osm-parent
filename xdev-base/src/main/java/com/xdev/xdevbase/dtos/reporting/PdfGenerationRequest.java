package com.xdev.xdevbase.dtos.reporting;

import java.util.List;
import java.util.Map;

public class PdfGenerationRequest {
    private String templateName;
    private Map<String, Object> parameters;
    private List<Map<String, Object>> dataSource;
    private String outputFileName;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<Map<String, Object>> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<Map<String, Object>> dataSource) {
        this.dataSource = dataSource;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}