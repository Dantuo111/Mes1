package com.app.mapper;

import com.app.entity.MaterialRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MaterialRequestMapper {
    List<MaterialRequest> selectList(@Param("userId") Long userId, @Param("status") Integer status);
    MaterialRequest selectById(Long id);
    int insert(MaterialRequest request);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("reply") String reply);
}
