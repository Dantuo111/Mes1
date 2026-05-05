package com.app.service;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.Material;
import com.app.entity.MaterialRecord;
import com.app.mapper.MaterialMapper;
import com.app.mapper.MaterialRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private MaterialRecordMapper materialRecordMapper;

    /**
     * 获取物料列表（分页，支持关键字搜索）
     */
    public Result getMaterialList(String keyword, Integer pageNum, Integer pageSize) {
        List<Material> allList = materialMapper.selectList(keyword);
        int total = allList.size();

        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Material> pageList;
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
     * 获取物料详情
     */
    public Result getMaterialDetail(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            return Result.error("物料不存在");
        }
        return Result.success(material);
    }

    /**
     * 新增或编辑物料
     */
    public Result saveMaterial(Material material) {
        if (material.getId() == null) {
            // 新增：校验物料编码唯一性
            Material existing = materialMapper.selectByCode(material.getCode());
            if (existing != null) {
                return Result.error("物料编码已存在");
            }
            materialMapper.insert(material);
        } else {
            // 编辑
            materialMapper.updateById(material);
        }
        return Result.success();
    }

    /**
     * 删除物料
     */
    public Result deleteMaterial(Long id) {
        materialMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 入库操作
     */
    @Transactional
    public Result inbound(Long materialId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error("入库数量必须大于0");
        }
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return Result.error("物料不存在");
        }

        int beforeQuantity = material.getStockQuantity();
        int afterQuantity = beforeQuantity + quantity;

        // 更新库存
        materialMapper.updateStockQuantity(materialId, afterQuantity);

        // 创建入库记录
        MaterialRecord record = new MaterialRecord();
        record.setMaterialId(materialId);
        record.setType(Constants.MATERIAL_INBOUND);
        record.setQuantity(quantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setOperateTime(new Date());
        materialRecordMapper.insert(record);

        return Result.success();
    }

    /**
     * 出库操作
     */
    @Transactional
    public Result outbound(Long materialId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error("出库数量必须大于0");
        }
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            return Result.error("物料不存在");
        }

        int beforeQuantity = material.getStockQuantity();
        if (beforeQuantity < quantity) {
            return Result.error("库存不足");
        }

        int afterQuantity = beforeQuantity - quantity;

        // 更新库存
        materialMapper.updateStockQuantity(materialId, afterQuantity);

        // 创建出库记录
        MaterialRecord record = new MaterialRecord();
        record.setMaterialId(materialId);
        record.setType(Constants.MATERIAL_OUTBOUND);
        record.setQuantity(quantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setOperateTime(new Date());
        materialRecordMapper.insert(record);

        return Result.success();
    }

    /**
     * 获取库存预警列表
     */
    public Result getWarningList() {
        List<Material> warningList = materialMapper.selectWarningList();
        return Result.success(warningList);
    }

    /**
     * 获取出入库记录（分页）
     */
    public Result getRecords(Long materialId, Integer pageNum, Integer pageSize) {
        List<MaterialRecord> allRecords;
        if (materialId != null) {
            allRecords = materialRecordMapper.selectByMaterialId(materialId);
        } else {
            allRecords = materialRecordMapper.selectAll();
        }

        int total = allRecords.size();

        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<MaterialRecord> pageList;
        if (fromIndex >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allRecords.subList(fromIndex, toIndex);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }
}
