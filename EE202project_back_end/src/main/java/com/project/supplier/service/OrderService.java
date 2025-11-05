package com.project.supplier.service;
import com.project.supplier.model.PurchaseOrder;
import com.project.supplier.model.PurchaseOrderItem;
import com.project.supplier.model.Supplier;

import jakarta.transaction.Transactional;

import com.project.depot.dao.InboundReceiptRepository;
import com.project.supplier.dao.OrderItemRepository;
import com.project.supplier.dao.OrderRepository;
import com.project.supplier.dao.SupplierRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 訂單服務類別
 * 提供訂單相關的業務邏輯操作。
 */
@Service
public class OrderService {
	
	@Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InboundReceiptRepository inboundReceiptRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 新增訂單。
     * @param supplierId 供應商ID
     * @param orderDate 訂單日期
     * @param status 訂單狀態
     * @param subTotal 訂單總金額 (此參數在方法內部重新計算)
     * @param materialIds 物料ID列表
     * @param quantities 數量列表
     * @param unitPrices 單價列表
     * @return 新增訂單的ID
     * @throws Exception 如果找不到供應商
     */
    @Transactional
    public int insertOrder(int supplierId, String orderDate, String status, BigDecimal subTotal,  List<String>materialIds, List<BigDecimal>quantities, List<String> unitPrices ) throws Exception {
		Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(
                () -> new Exception("找不到供應商 ID=" + supplierId)
        );
        //訂單主檔
		PurchaseOrder order = new PurchaseOrder();
		order.setSupplier(supplier);
		order.setOrderDate(orderDate);
		order.setOrderStatus(status);
        //subTotal
        subTotal = BigDecimal.ZERO;
        for (int i = 0; i < materialIds.size(); i++) {
        BigDecimal quantity = quantities.get(i);
        BigDecimal unitPrice = new BigDecimal(unitPrices.get(i));
        subTotal = subTotal.add(quantity.multiply(unitPrice));
        }
        order.setSubTotal(subTotal.doubleValue());

        // 儲存訂單
        PurchaseOrder savedOrder = orderRepository.save(order);

        //訂單明細
        for (int i = 0; i < materialIds.size(); i++) {
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMaterialId(Integer.parseInt(materialIds.get(i)));
            orderItem.setQuantity(quantities.get(i));
            orderItem.setUnitPrice(Double.parseDouble(unitPrices.get(i)));
            orderItem.setDeliveryStatus("出貨中");
            orderItemRepository.save(orderItem);
        }
        return savedOrder.getOrderId();
    }
    
    /**
     * 根據訂單ID查詢單筆訂單。
     * @param orderId 訂單ID
     * @return 訂單實體
     * @throws Exception 如果找不到訂單
     */
    public PurchaseOrder getOrderById(int orderId) throws Exception {
        PurchaseOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("找不到訂單 ID=" + orderId);
        }
        List<PurchaseOrderItem> orderItems = orderItemRepository.findByOrder(order);
        order.setItemList(orderItems);
        return order;
    }
    
    /**
     * 查詢所有訂單及其明細。
     * 訂單依 orderId 遞減排序。
     * @return 所有訂單的列表
     */
    public List<PurchaseOrder> getAllOrdersWithItems() {
        //查詢全部的 Order 資料，並依照 orderId 欄位進行遞減排序（從大到小）。
        List<PurchaseOrder> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"));
        for (PurchaseOrder order : orders) {
            List<PurchaseOrderItem> orderItems = orderItemRepository.findByOrder(order);
            order.setItemList(orderItems);
        }
        return orders;
    }

    /**
     * 刪除訂單及其所有明細。
     * @param orderId 訂單ID
     * @throws Exception 如果找不到訂單
     */
    @Transactional
    public void deleteOrder(int orderId) throws Exception {
        PurchaseOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("找不到訂單 ID=" + orderId);
        }
       List<PurchaseOrderItem> orderItems = orderItemRepository.findByOrder(order);
        orderItemRepository.deleteAll(orderItems);
        orderRepository.delete(order);
    }  

    /**
     * 更新訂單及其明細。
     * 更新訂單時，會先刪除原有的訂單明細，然後新增新的訂單明細。
     * @param orderId 訂單ID
     * @param supplierId 供應商ID
     * @param orderDate 訂單日期
     * @param status 訂單狀態
     * @param subTotal 訂單總金額 (此參數在方法內部重新計算)
     * @param materialIds 物料ID列表
     * @param quantities 數量列表
     * @param unitPrices 單價列表
     * @throws Exception 如果找不到訂單或供應商
     */
    @Transactional
    public void updateOrder(int orderId, int supplierId, String orderDate, String status, BigDecimal subTotal, List<String> materialIds, List<BigDecimal> quantities, List<String> unitPrices) throws Exception {
        PurchaseOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Exception("找不到訂單 ID=" + orderId);
        }
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null) {
            throw new Exception("找不到供應商 ID=" + supplierId);
        }
        order.setSupplier(supplier);
        order.setOrderDate(orderDate);
        order.setOrderStatus(status);
        orderRepository.save(order);
        // 刪除原有的訂單明細
        orderItemRepository.deleteAll(orderItemRepository.findByOrder(order));
        // 新增新的訂單明細
        subTotal = BigDecimal.ZERO;
        for (int i = 0; i < materialIds.size(); i++) {
            //計算subTotal
            BigDecimal quantity = quantities.get(i);
            BigDecimal unitPrice = new BigDecimal(unitPrices.get(i));
            subTotal = subTotal.add(quantity.multiply(unitPrice));

            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setOrder(order);// 設定訂單關聯
            orderItem.setMaterialId(Integer.parseInt(materialIds.get(i)));
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(Double.parseDouble(unitPrices.get(i)));
            orderItem.setDeliveryStatus("出貨中");
            orderItemRepository.save(orderItem);
        }
        order.setSubTotal(subTotal.doubleValue());
        orderRepository.save(order); 
    }

    /**
     * 取得每月採購總金額統計。
     * @return 包含每月採購金額的列表
     */
    public List<Map<String, Object>> getMonthlyPurchaseTotal(){
        return orderRepository.findMonthlyTotal();
    }

    /**
     * 取得各供應商採購佔比統計。
     * @return 包含各供應商採購佔比的列表
     */
    public List<Map<String, Object>> getSupplierPurchaseTotal(){
        return orderRepository.findSupplierTotal();
    }   
}