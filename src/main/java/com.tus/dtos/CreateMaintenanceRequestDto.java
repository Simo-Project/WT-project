package com.tus.dtos;

import com.tus.db.models.RequestCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateMaintenanceRequestDto {

    @NotBlank(message = "Title is required")
    private String title; // maps to entity.task

    @NotNull(message = "Category is required")
    private RequestCategory category;

    @NotBlank(message = "Description is required")
    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public RequestCategory getCategory() { return category; }
    public void setCategory(RequestCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}