package com.app.mapper;

import com.app.entity.ProductionPlan;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductionPlanMapper {

    ProductionPlan selectById(Long id);

    List<ProductionPlan> selectList(@Param("keyword") String keyword, @Param("status") Integer status);

    int insert(ProductionPlan plan);

    int updateById(ProductionPlan plan);

    int deleteById(Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int countAll();
}
