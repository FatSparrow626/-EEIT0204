package com.project.workorder.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 用於更新工單已生產數量的請求DTO。
 */
@Data
public class ProducedQuantityRequest {
    private BigDecimal successfulQuantity;
    private BigDecimal failedQuantity;
}
