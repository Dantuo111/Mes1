package com.app.controller;

import com.app.common.Result;
import com.app.entity.QualityInspection;
import com.app.service.QualityInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quality")
public class QualityInspectionController {

    @Autowired
    private QualityInspectionService qualityInspectionService;

    /**
     * 检验记录列表（分页，支持筛选）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long workOrderId,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Double minPassRate,
                       @RequestParam(required = false) Double maxPassRate,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return qualityInspectionService.getInspectionList(workOrderId, startDate, endDate,
                minPassRate, maxPassRate, pageNum, pageSize);
    }

    /**
     * 检验记录详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return qualityInspectionService.getInspectionDetail(id);
    }

    /**
     * 新增/编辑检验记录
     */
    @PostMapping("/save")
    public Result save(@RequestBody QualityInspection inspection) {
        return qualityInspectionService.saveInspection(inspection);
    }

    /**
     * 删除检验记录
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return qualityInspectionService.deleteInspection(id);
    }

    /**
     * 质量统计数据（合格率趋势、不合格原因分布）
     */
    @GetMapping("/stats")
    public Result stats() {
        return qualityInspectionService.getStats();
    }
}
