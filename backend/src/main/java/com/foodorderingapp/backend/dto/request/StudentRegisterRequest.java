package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentRegisterRequest extends BaseRegisterRequest {
    @NotNull(message = "Please select the dormitory building")
    private UUID buildingId;
}
