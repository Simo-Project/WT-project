package com.tus.db.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRequestDefaultsUnitTest {

    @Test
    void onCreate_setsDefaultStatusNew_whenMissing() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Leaking tap");
        mr.setUnit("Apt 12");
        mr.setCategory(RequestCategory.PLUMBING);
        mr.setDescription("Kitchen tap is dripping");

        mr.onCreate(); // simulate @PrePersist

        assertEquals(RequestStatus.NEW, mr.getStatus());
        assertNotNull(mr.getCreatedOn());
    }

    @Test
    void onCreate_doesNotOverwriteProvidedValues() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Paint");
        mr.setUnit("Apt 8");
        mr.setCategory(RequestCategory.OTHER);
        mr.setDescription("Small patch");
        mr.setStatus(RequestStatus.CLOSED);
        mr.setCreatedOn(LocalDate.of(2026, 2, 28));

        mr.onCreate();

        assertEquals(RequestStatus.CLOSED, mr.getStatus());
        assertEquals(LocalDate.of(2026, 2, 28), mr.getCreatedOn());
    }
}