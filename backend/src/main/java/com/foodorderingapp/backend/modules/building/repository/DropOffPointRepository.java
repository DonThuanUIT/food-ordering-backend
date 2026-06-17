package com.foodorderingapp.backend.modules.building.repository;

import com.foodorderingapp.backend.entity.DropOffPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DropOffPointRepository extends JpaRepository<DropOffPoint, UUID> {
    List<DropOffPoint> findByBuildingId(UUID buildingId);
}