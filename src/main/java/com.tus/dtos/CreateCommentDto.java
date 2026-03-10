package com.tus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentDto {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 1000, message = "Comment must be 1000 characters or less")
    private String text;

    public CreateCommentDto() {
    }

    public CreateCommentDto(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}