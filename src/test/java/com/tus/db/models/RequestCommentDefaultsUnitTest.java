package com.tus.db.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestCommentDefaultsUnitTest {

    @Test
    void onCreate_setsCreatedAt_whenNull() {
        RequestComment comment = new RequestComment();
        comment.setText("Please call before arriving.");
        comment.setCreatedAt(null);

        comment.onCreate();

        assertNotNull(comment.getCreatedAt());
    }

    @Test
    void onCreate_doesNotOverwriteExistingCreatedAt() {
        RequestComment comment = new RequestComment();
        LocalDateTime fixedTime = LocalDateTime.of(2026, 3, 14, 17, 0);
        comment.setCreatedAt(fixedTime);

        comment.onCreate();

        assertEquals(fixedTime, comment.getCreatedAt());
    }

    @Test
    void constructor_setsRequestAuthorAndText() {
        MaintenanceRequest request = new MaintenanceRequest();
        AppUser author = new AppUser();

        RequestComment comment = new RequestComment(request, author, "Engineer booked.");

        assertSame(request, comment.getRequest());
        assertSame(author, comment.getAuthor());
        assertEquals("Engineer booked.", comment.getText());
    }
}