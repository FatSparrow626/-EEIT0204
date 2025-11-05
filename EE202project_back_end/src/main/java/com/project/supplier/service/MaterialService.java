package com.project.supplier.service;
import com.project.depot.model.Material;
import com.project.supplier.dao.MaterialMesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 物料服務類別
 * 提供物料相關的業務邏輯操作。
 */
@Service
public class MaterialService {
	
	@Autowired
    private MaterialMesRepository materialRepository;
    
    /**
     * 取得所有啟用狀態 (active = 1) 的物料。
     * @return 啟用物料的列表
     */
    public List<Material> getActiveMaterials() {
        return materialRepository.findByActiveTrueOrderByMaterialId();  
	}

    /**
     * 取得指定父物料 (parentMaterialId) 的 BOM 中未使用的啟用狀態物料。
     * @param parentMaterialId 父物料的ID
     * @return 未使用的物料列表
     */
    public List<Material> getUnusedMaterials(Long parentMaterialId) {
        return materialRepository.findUnusedMaterials(parentMaterialId);
    }
}