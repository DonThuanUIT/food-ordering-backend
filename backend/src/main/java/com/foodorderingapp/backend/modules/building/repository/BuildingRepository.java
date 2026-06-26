package com.foodorderingapp.backend.modules.building.repository;

import com.foodorderingapp.backend.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {
    java.util.Optional<Building> findByName(String name);
}