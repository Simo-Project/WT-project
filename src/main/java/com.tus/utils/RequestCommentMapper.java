package com.tus.utils;

import com.tus.db.models.RequestComment;
import com.tus.dtos.RequestCommentDto;

public class RequestCommentMapper {

    public static RequestCommentDto toDto(RequestComment comment) {
        return new RequestCommentDto(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }
}