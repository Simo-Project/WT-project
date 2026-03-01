package com.tus.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateMaintenanceRequestDtoValidationUnitTest {

    @Test
    void validation_failsWhenRequiredFieldsMissing() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setTitle("");          // NotBlank should fail
        dto.setCategory(null);     // NotNull should fail
        dto.setDescription("");    // NotBlank should fail

        Set<ConstraintViolation<CreateMaintenanceRequestDto>> violations = validator.validate(dto);

        assertEquals(3, violations.size());

        // Optional: check which fields failed (more readable)
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("category")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void validationPassesWhenFieldsPresent() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setTitle("Leaking tap");
        dto.setCategory(com.tus.db.models.RequestCategory.PLUMBING);
        dto.setDescription("Kitchen tap is dripping");

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}