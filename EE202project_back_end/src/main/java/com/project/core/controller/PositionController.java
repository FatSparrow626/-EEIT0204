package com.project.core.controller;

import com.project.core.dao.PositionRepository;
import com.project.core.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Tag(name = "職位管理", description = "職位資料查詢")
@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    @Autowired
    private PositionRepository positionRepository;

    /**
     * 獲取所有職位列表
     * @return 職位列表
     */
    @Operation(summary = "獲取所有職位", description = "返回系統中所有可用的職位列表")
    @GetMapping
    public ResponseEntity<?> getAllPositions() {
        try {
            logger.info("開始獲取所有職位列表");
            
            List<Position> positions = positionRepository.findAll();
            
            logger.info("成功獲取 {} 個職位", positions.size());
            return ResponseEntity.ok(positions);
            
        } catch (Exception e) {
            logger.error("獲取職位列表時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("獲取職位列表失敗: " + e.getMessage());
        }
    }

    /**
     * 根據職位ID獲取職位資訊
     * @param id 職位ID
     * @return 職位資訊
     */
    @Operation(summary = "根據ID獲取職位", description = "根據職位ID返回特定職位的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<?> getPositionById(@Parameter(description = "職位ID", required = true) @PathVariable Integer id) {
        try {
            logger.info("開始獲取職位ID: {}", id);
            
            return positionRepository.findById(id)
                    .<ResponseEntity<?>>map(position -> {
                        logger.info("成功找到職位: {}", position.getPositionName());
                        return ResponseEntity.ok(position);
                    })
                    .orElseGet(() -> {
                        logger.warn("找不到職位ID: {}", id);
                        return ResponseEntity.status(404)
                                .body("找不到職位ID: " + id);
                    });
                    
        } catch (Exception e) {
            logger.error("獲取職位資訊時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("獲取職位資訊失敗: " + e.getMessage());
        }
    }

    /**
     * 根據職位名稱搜尋職位
     * @param name 職位名稱
     * @return 職位資訊
     */
    @Operation(summary = "根據名稱搜尋職位", description = "根據職位名稱搜尋特定職位的詳細資訊")
    @GetMapping("/search")
    public ResponseEntity<?> searchPositionByName(
            @Parameter(description = "職位名稱", required = true) @RequestParam("name") String name) {
        try {
            logger.info("開始搜尋職位名稱: {}", name);
            
            if (name == null || name.trim().isEmpty()) {
                logger.warn("職位名稱參數為空");
                return ResponseEntity.badRequest()
                        .body("職位名稱不能為空");
            }
            
            Optional<Position> position = positionRepository.findByPositionName(name.trim());
            
            if (position.isPresent()) {
                logger.info("成功找到職位: {}", position.get().getPositionName());
                return ResponseEntity.ok(position.get());
            } else {
                logger.info("找不到職位名稱: {}", name);
                return ResponseEntity.status(404)
                        .body("找不到職位名稱: " + name);
            }
                    
        } catch (Exception e) {
            logger.error("搜尋職位時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("搜尋職位失敗: " + e.getMessage());
        }
    }
}