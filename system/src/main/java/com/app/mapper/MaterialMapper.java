package com.app.mapper;

import com.app.entity.Material;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MaterialMapper {

    Material selectById(Long id);

    Material selectByCode(String code);

    List<Material> selectList(@Param("keyword") String keyword);

    List<Material> selectWarningList();

    int insert(Material material);

    int updateById(Material material);

    int deleteById(Long id);

    int updateStockQuantity(@Param("id") Long id, @Param("stockQuantity") Integer stockQuantity);

    int countAll();
}
