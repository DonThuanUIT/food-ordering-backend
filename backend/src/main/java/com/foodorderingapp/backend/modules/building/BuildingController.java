package com.foodorderingapp.backend.modules.building;

import com.foodorderingapp.backend.modules.building.dto.response.BuildingResponse;
import com.foodorderingapp.backend.modules.building.dto.response.DropOffPointResponse;
import com.foodorderingapp.backend.modules.building.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<List<BuildingResponse>> getAllBuildings() {
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    @GetMapping("/{buildingId}/drop-off-points")
    public ResponseEntity<List<DropOffPointResponse>> getDropOffPoints(
            @PathVariable UUID buildingId) {
        return ResponseEntity.ok(buildingService.getDropOffPointsByBuilding(buildingId));
    }
}