package com.project.workorder.dao;

import com.project.workorder.model.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 工單 - 物料清單 (BOM) DAO (Repository)
 */
@Repository
public interface BomComponentRepository extends JpaRepository<BomComponent, Integer> {
}