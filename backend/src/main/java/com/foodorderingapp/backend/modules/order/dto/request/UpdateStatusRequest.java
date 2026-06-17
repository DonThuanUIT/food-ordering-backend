package com.foodorderingapp.backend.modules.order.dto.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdateStatusRequest {
    private String status;
    private String cancelReason;
}
