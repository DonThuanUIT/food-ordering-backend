package com.foodorderingapp.backend.modules.building;

import com.foodorderingapp.backend.modules.building.dto.response.BuildingResponse;
import com.foodorderingapp.backend.modules.building.dto.response.DropOffPointResponse;
import com.foodorderingapp.backend.entity.Building;
import com.foodorderingapp.backend.entity.DropOffPoint;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.building.repository.BuildingRepository;
import com.foodorderingapp.backend.modules.building.repository.DropOffPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final DropOffPointRepository dropOffPointRepository;

    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(b -> BuildingResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .latitude(b.getLatitude())
                        .longitude(b.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DropOffPointResponse> getDropOffPointsByBuilding(UUID buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new AppException("Building not found", HttpStatus.NOT_FOUND));

        return dropOffPointRepository.findByBuildingId(buildingId).stream()
                .map(d -> DropOffPointResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .buildingId(building.getId())
                        .build())
                .collect(Collectors.toList());
    }
}