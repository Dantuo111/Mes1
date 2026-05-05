package com.app.service;

import com.app.common.Result;
import com.app.entity.QualityInspection;
import com.app.mapper.QualityInspectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QualityInspectionService {

    @Autowired
    private QualityInspectionMapper qualityInspectionMapper;

    /**
     * 获取检验记录列表（分页，支持筛选）
     */
    public Result getInspectionList(Long workOrderId, String startDate, String endDate,
                                     Double minPassRate, Double maxPassRate,
                                     Integer pageNum, Integer pageSize) {
        List<QualityInspection> allList = qualityInspectionMapper.selectList(
                workOrderId, startDate, endDate, minPassRate, maxPassRate);
        int total = allList.size();

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<QualityInspection> pageList;
        if (fromIndex >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allList.subList(fromIndex, toIndex);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    /**
     * 获取检验记录详情
     */
    public Result getInspectionDetail(Long id) {
        QualityInspection inspection = qualityInspectionMapper.selectById(id);
        if (inspection == null) {
            return Result.error("检验记录不存在");
        }
        return Result.success(inspection);
    }

    /**
     * 新增或编辑检验记录
     */
    public Result saveInspection(QualityInspection inspection) {
        // 数量非负校验
        if (inspection.getInspectionQuantity() != null && inspection.getInspectionQuantity() <= 0) {
            return Result.error("检验数量必须大于0");
        }
        if (inspection.getQualifiedQuantity() != null && inspection.getQualifiedQuantity() < 0) {
            return Result.error("合格数量不能为负数");
        }
        if (inspection.getUnqualifiedQuantity() != null && inspection.getUnqualifiedQuantity() < 0) {
            return Result.error("不合格数量不能为负数");
        }
        // 数量一致性校验
        if (inspection.getQualifiedQuantity() != null && inspection.getUnqualifiedQuantity() != null
                && inspection.getInspectionQuantity() != null) {
            if (inspection.getQualifiedQuantity() + inspection.getUnqualifiedQuantity()
                    != inspection.getInspectionQuantity()) {
                return Result.error("数量不一致");
            }
        }

        // 自动计算合格率
        if (inspection.getInspectionQuantity() != null && inspection.getInspectionQuantity() > 0
                && inspection.getQualifiedQuantity() != null) {
            double passRate = (double) inspection.getQualifiedQuantity() / inspection.getInspectionQuantity() * 100;
            passRate = Math.round(passRate * 100.0) / 100.0;
            inspection.setPassRate(passRate);
        }

        if (inspection.getId() == null) {
            qualityInspectionMapper.insert(inspection);
        } else {
            qualityInspectionMapper.updateById(inspection);
        }
        return Result.success();
    }

    /**
     * 删除检验记录
     */
    public Result deleteInspection(Long id) {
        qualityInspectionMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 获取质量统计数据
     */
    public Result getStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("passRateTrend", qualityInspectionMapper.selectPassRateTrend());
        data.put("unqualifiedReasonStats", qualityInspectionMapper.selectUnqualifiedReasonStats());
        return Result.success(data);
    }
}
