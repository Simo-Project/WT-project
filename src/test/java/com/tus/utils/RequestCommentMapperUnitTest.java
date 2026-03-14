package com.tus.utils;

import com.tus.db.models.AppUser;
import com.tus.db.models.RequestComment;
import com.tus.db.models.UserRole;
import com.tus.dtos.RequestCommentDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestCommentMapperUnitTest {

    @Test
    void toDto_mapsAuthorTextAndCreatedAt() {
        AppUser author = new AppUser();
        author.setUsername("resident");
        author.setRole(UserRole.RESIDENT);

        RequestComment comment = new RequestComment();
        comment.setAuthor(author);
        comment.setText("Please call before arriving.");
        comment.setCreatedAt(LocalDateTime.of(2026, 3, 14, 16, 30));

        RequestCommentDto dto = RequestCommentMapper.toDto(comment);

        assertEquals("resident", dto.getAuthorUsername());
        assertEquals("Please call before arriving.", dto.getText());
        assertEquals(LocalDateTime.of(2026, 3, 14, 16, 30), dto.getCreatedAt());
    }

    @Test
    void toDto_allowsNullCreatedAt() {
        AppUser author = new AppUser();
        author.setUsername("admin");
        author.setRole(UserRole.ADMIN);

        RequestComment comment = new RequestComment();
        comment.setAuthor(author);
        comment.setText("Engineer booked.");
        comment.setCreatedAt(null);

        RequestCommentDto dto = RequestCommentMapper.toDto(comment);

        assertEquals("admin", dto.getAuthorUsername());
        assertEquals("Engineer booked.", dto.getText());
        assertNull(dto.getCreatedAt());
    }
}