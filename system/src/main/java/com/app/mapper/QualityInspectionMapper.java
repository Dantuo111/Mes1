package com.app.mapper;

import com.app.entity.QualityInspection;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface QualityInspectionMapper {

    QualityInspection selectById(Long id);

    List<QualityInspection> selectList(@Param("workOrderId") Long workOrderId,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate,
                                       @Param("minPassRate") Double minPassRate,
                                       @Param("maxPassRate") Double maxPassRate);

    int insert(QualityInspection inspection);

    int updateById(QualityInspection inspection);

    int deleteById(Long id);

    // 统计：按月份统计合格率趋势
    List<Map<String, Object>> selectPassRateTrend();

    // 统计：不合格原因分布
    List<Map<String, Object>> selectUnqualifiedReasonStats();
}
