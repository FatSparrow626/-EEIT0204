package com.project.workorder.model;

import com.project.employeeuser.model.EmployeeUser;
import com.project.depot.model.Material;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

/**
 * 工單 - 工單主檔實體類別
 * 對應資料表: outbound_work_orders
 */
@Entity
@Table(name = "outbound_work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wo_id")
    private Long woId; // 工單ID (主鍵)

    @Column(name = "wo_number", nullable = false, unique = true, length = 100)
    private String woNumber; // 工單號碼 (唯一)

    @ManyToOne(fetch = FetchType.EAGER) // 將 FetchType 改為 EAGER，確保物料資訊在查詢工單時一併載入
    @JoinColumn(name = "material_id", nullable = false)
    private Material material; // 要生產的成品/半成品

    @Column(name = "required_quantity", nullable = false)
    private java.math.BigDecimal requiredQuantity; // 需求數量

    @Column(name = "produced_quantity", nullable = true)
    private java.math.BigDecimal producedQuantity; // 已生產數量

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // 狀態

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private EmployeeUser requestedBy; // 需求人員

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id")
    private EmployeeUser issuedBy; // 開單人員

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 建立時間

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 更新時間

    @Column(name = "failed_quantity")
    private BigDecimal failedQuantity; // 失敗數量

    @Column(name = "successful_quantity")
    private BigDecimal successfulQuantity; // 成功數量

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getWoId() {
        return woId;
    }

    public void setWoId(Long woId) {
        this.woId = woId;
    }

    public String getWoNumber() {
        return woNumber;
    }

    public void setWoNumber(String woNumber) {
        this.woNumber = woNumber;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(BigDecimal requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EmployeeUser getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(EmployeeUser requestedBy) { // 修正類型為 EmployeeUser
        this.requestedBy = requestedBy;
    }

    public EmployeeUser getIssuedBy() { // 修正類型為 EmployeeUser
        return issuedBy;
    }

    public void setIssuedBy(EmployeeUser issuedBy) {
        this.issuedBy = issuedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getFailedQuantity() {
        return failedQuantity;
    }

    public void setFailedQuantity(BigDecimal failedQuantity) {
        this.failedQuantity = failedQuantity;
    }

    public BigDecimal getSuccessfulQuantity() {
        return successfulQuantity;
    }

    public void setSuccessfulQuantity(BigDecimal successfulQuantity) {
        this.successfulQuantity = successfulQuantity;
    }
}