package com.app.mapper;

import com.app.entity.QualityFeedback;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface QualityFeedbackMapper {
    List<QualityFeedback> selectList(@Param("userId") Long userId, @Param("status") Integer status);
    QualityFeedback selectById(Long id);
    int insert(QualityFeedback feedback);
    int updateById(QualityFeedback feedback);
}
