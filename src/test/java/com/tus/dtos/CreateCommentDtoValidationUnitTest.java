package com.tus.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateCommentDtoValidationUnitTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validComment_hasNoViolations() {
        CreateCommentDto dto = new CreateCommentDto("Please call before arriving.");

        Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankComment_failsNotBlankValidation() {
        CreateCommentDto dto = new CreateCommentDto("   ");

        Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<CreateCommentDto> violation = violations.iterator().next();

        assertEquals("text", violation.getPropertyPath().toString());
        assertEquals("Comment cannot be empty", violation.getMessage());
    }

    @Test
    void tooLongComment_failsSizeValidation() {
        String longText = "a".repeat(1001);
        CreateCommentDto dto = new CreateCommentDto(longText);

        Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<CreateCommentDto> violation = violations.iterator().next();

        assertEquals("text", violation.getPropertyPath().toString());
        assertEquals("Comment must be 1000 characters or less", violation.getMessage());
    }
}