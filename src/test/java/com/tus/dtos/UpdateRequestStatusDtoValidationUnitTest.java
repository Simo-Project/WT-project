package com.tus.dtos;

import com.tus.db.models.RequestStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateRequestStatusDtoValidationUnitTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validStatus_hasNoViolations() {
        UpdateRequestStatusDto dto = new UpdateRequestStatusDto();
        dto.setStatus(RequestStatus.IN_PROGRESS);

        Set<ConstraintViolation<UpdateRequestStatusDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullStatus_failsNotNullValidation() {
        UpdateRequestStatusDto dto = new UpdateRequestStatusDto();
        dto.setStatus(null);

        Set<ConstraintViolation<UpdateRequestStatusDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<UpdateRequestStatusDto> violation = violations.iterator().next();

        assertEquals("status", violation.getPropertyPath().toString());
        assertNotNull(violation.getMessage());
    }
}