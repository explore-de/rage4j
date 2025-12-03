package dev.rage4j.demo.model;

import java.util.List;

public class MetricInfo {
    private String id;
    private String name;
    private String description;
    private List<String> requiredFields;

    public MetricInfo() {}

    public MetricInfo(String id, String name, String description, List<String> requiredFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredFields = requiredFields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }
}
