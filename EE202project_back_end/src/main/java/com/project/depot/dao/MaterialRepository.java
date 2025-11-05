package com.project.depot.dao;

import com.project.depot.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 倉庫 - 物料 DAO (Repository)
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByMaterialType(String materialType);
//判斷低於安全庫存
    @Query("SELECT m FROM Material m WHERE m.stockCurrent < m.safetyStock")
    List<Material> findByStockCurrentLessThanSafeStock();

}