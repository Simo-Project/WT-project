package com.tus.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerUnitTest {

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidation_returnsMessageAndFieldErrors() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("createCommentDto", "text", "Comment cannot be empty"),
                new FieldError("updateRequestStatusDto", "status", "must not be null")
        ));

        Map<String, Object> result = handler.handleValidation(validationException);

        assertEquals("Validation failed", result.get("message"));
        assertTrue(result.containsKey("errors"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) result.get("errors");

        assertEquals(2, errors.size());
        assertEquals("Comment cannot be empty", errors.get("text"));
        assertEquals("must not be null", errors.get("status"));
    }

    @Test
    void handleValidation_overwritesDuplicateFieldWithLastMessage() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "text", "First message"),
                new FieldError("dto", "text", "Second message")
        ));

        Map<String, Object> result = handler.handleValidation(validationException);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) result.get("errors");

        assertEquals(1, errors.size());
        assertEquals("Second message", errors.get("text"));
    }

    @Test
    void handleResponseStatus_returnsBodyWithReasonAndStatus() {
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled requests cannot be updated");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cancelled requests cannot be updated", response.getBody().get("message"));
        assertEquals(409, response.getBody().get("status"));
    }

    @Test
    void handleResponseStatus_usesFallbackMessageWhenReasonIsNull() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request failed", response.getBody().get("message"));
        assertEquals(400, response.getBody().get("status"));
    }
}
