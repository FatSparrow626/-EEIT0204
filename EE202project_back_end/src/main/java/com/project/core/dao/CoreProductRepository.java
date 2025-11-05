package com.project.core.dao;

import com.project.depot.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreProductRepository extends JpaRepository<Material, Long> {
    void deleteByMaterialId(Long materialId);
}
