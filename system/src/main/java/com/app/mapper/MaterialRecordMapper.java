package com.app.mapper;

import com.app.entity.MaterialRecord;
import java.util.List;

public interface MaterialRecordMapper {

    List<MaterialRecord> selectByMaterialId(Long materialId);

    List<MaterialRecord> selectAll();

    int insert(MaterialRecord record);
}
