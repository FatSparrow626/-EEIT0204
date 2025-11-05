package com.project.depot.model;

import com.project.employeeuser.model.EmployeeUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 倉庫 - 庫存交易紀錄實體類別
 * 對應資料表: inventory_transactions
 */
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    /**
     * 交易ID (主鍵)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId; 

    /**
     * 物料 (關聯至 Material 實體)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material; 

    /**
     * 交易類型
     */
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; 

    /**
     * 異動數量 (可正可負)
     */
    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity; 

    /**
     * 交易日期
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate; 

    /**
     * 來源表名
     */
    @Column(name = "reference_table", length = 100)
    private String referenceTable; 

    /**
     * 來源表的主鍵 ID
     */
    @Column(name = "reference_id")
    private Long referenceId; 

    /**
     * 建立者 (關聯至 EmployeeUser 實體)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private EmployeeUser createdBy; 

    /**
     * 備註
     */
    @Lob
    @Column(name = "notes")
    private String notes; 

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public EmployeeUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeUser createdBy) {
        this.createdBy = createdBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}