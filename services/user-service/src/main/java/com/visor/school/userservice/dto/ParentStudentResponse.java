package com.visor.school.userservice.dto;

import java.util.UUID;

import com.visor.school.userservice.model.Parent;

public record ParentStudentResponse(
    UUID parentId,
    UUID studentId,
    String relationship,
    boolean isPrimary
) {
    public static ParentStudentResponse from(Parent parent) {
        return new ParentStudentResponse(
            parent.getUserId(),
            parent.getStudentId(),
            parent.getRelationship().name(),
            parent.isPrimary()
        );
    }
}
