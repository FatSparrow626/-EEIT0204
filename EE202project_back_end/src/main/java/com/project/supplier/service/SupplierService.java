package com.project.supplier.service;
import com.project.supplier.model.Supplier;
import com.project.supplier.dao.SupplierRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 供應商服務類別
 * 提供供應商相關的業務邏輯操作。
 */
@Service
public class SupplierService {
	
	@Autowired
	private  SupplierRepository supplierRepository;
	
	/**
	 * 取得所有啟用狀態 (active = 1) 的供應商。
	 * @return 啟用供應商的列表
	 */
    public  List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByActiveTrueOrderBySupplierId();
    }

    /**
     * 下架供應商（將 active 設為 0）。
     * @param supplierId 供應商ID
     */
    public void deactivateSupplier(int supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if(supplier != null){
            supplier.setActive(false);
            supplierRepository.save(supplier);
        }       
    }
    
    /**
     * 新增供應商。
     * @param name 供應商名稱
     * @param pm 聯絡人
     * @param phone 電話
     * @param email 電子郵件
     * @param address 地址
     * @param note 備註
     * @return 新增後的供應商實體
     */
    public Supplier insertSupplier(String name, String pm, String phone, String email, String address, String note)  {
    	Supplier s = new Supplier();
    		 s.setSupplierName(name);
    		 s.setPm(pm);
    		 s.setSupplierPhone(phone);
    		 s.setSupplierEmail(email);
    		 s.setSupplierAddress(address);
             s.setSupplierNote(note);
             s.setActive(true); //預設為1啟用
    		 supplierRepository.save(s); //使用JPA的save方法新增供應商
             return s;
    }
    
    /**
     * 更新供應商資訊。
     * @param id 供應商ID
     * @param name 供應商名稱
     * @param pm 聯絡人
     * @param phone 電話
     * @param email 電子郵件
     * @param address 地址
     * @param note 備註
     * @return 如果更新成功則返回 true，否則返回 false
     */
    public boolean updateSupplier(int id, String name, String pm, String phone, String email, String address, String note) {
       Supplier supplier = supplierRepository.findById(id).orElse(null);
       if(supplier != null){
             supplier.setSupplierName(name);
    		 supplier.setPm(pm);
    		 supplier.setSupplierPhone(phone);
    		 supplier.setSupplierEmail(email);
    		 supplier.setSupplierAddress(address);
             supplier.setSupplierNote(note);
             //supplier.setActive(true); //確保更新後仍為啟用狀態
			 supplierRepository.save(supplier); //使用JPA的save方法更新供應商
             return true;
       }
       return false;             
    }
    
    /**
     * 根據供應商ID查詢單筆供應商資訊。
     * @param supplierId 供應商ID
     * @return 供應商實體，如果找不到則返回 null
     */
    public Supplier getSupplierById(int supplierId) {
        return supplierRepository.findById(supplierId).orElse(null);
    }
}