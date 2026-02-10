package com.visor.school.userservice.dto;

import com.visor.school.userservice.model.AccountStatus;

public record UpdateStatusRequest(
    AccountStatus status
) {}
