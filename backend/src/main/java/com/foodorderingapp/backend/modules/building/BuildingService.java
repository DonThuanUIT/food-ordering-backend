package com.foodorderingapp.backend.modules.building;

import com.foodorderingapp.backend.modules.building.dto.response.BuildingResponse;
import com.foodorderingapp.backend.entity.Building;
import com.foodorderingapp.backend.modules.building.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;

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

    // >>> PHASE 1: AI Spatial - Tra cứu tọa độ tòa nhà KTX theo tên
    public Optional<BuildingResponse> findByBuildingName(String name) {
        return buildingRepository.findByName(name)
                .map(b -> BuildingResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .latitude(b.getLatitude())
                        .longitude(b.getLongitude())
                        .build());
    }
}
