package com.app.mapper;

import com.app.entity.WorkOrder;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface WorkOrderMapper {

    WorkOrder selectById(Long id);

    List<WorkOrder> selectList(@Param("status") Integer status, @Param("assigneeId") Long assigneeId);

    List<WorkOrder> selectByPlanId(Long planId);

    int insert(WorkOrder workOrder);

    int updateById(WorkOrder workOrder);

    int deleteById(Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int countAll();

    int countByStatus(Integer status);
}
