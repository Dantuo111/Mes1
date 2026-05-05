package com.app.controller;

import com.app.common.Result;
import com.app.entity.Material;
import com.app.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 物料列表（分页，支持关键字搜索）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return materialService.getMaterialList(keyword, pageNum, pageSize);
    }

    /**
     * 物料详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return materialService.getMaterialDetail(id);
    }

    /**
     * 新增/编辑物料
     */
    @PostMapping("/save")
    public Result save(@RequestBody Material material) {
        return materialService.saveMaterial(material);
    }

    /**
     * 删除物料
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return materialService.deleteMaterial(id);
    }

    /**
     * 入库操作
     */
    @PostMapping("/inbound")
    public Result inbound(@RequestBody Map<String, Object> params) {
        Long materialId = Long.valueOf(params.get("materialId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        return materialService.inbound(materialId, quantity);
    }

    /**
     * 出库操作
     */
    @PostMapping("/outbound")
    public Result outbound(@RequestBody Map<String, Object> params) {
        Long materialId = Long.valueOf(params.get("materialId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        return materialService.outbound(materialId, quantity);
    }

    /**
     * 出入库记录列表
     */
    @GetMapping("/records")
    public Result records(@RequestParam(required = false) Long materialId,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return materialService.getRecords(materialId, pageNum, pageSize);
    }

    /**
     * 库存预警列表
     */
    @GetMapping("/warning")
    public Result warning() {
        return materialService.getWarningList();
    }
}
