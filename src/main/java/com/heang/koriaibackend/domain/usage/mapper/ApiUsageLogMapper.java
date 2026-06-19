package com.heang.koriaibackend.domain.usage.mapper;

import com.heang.koriaibackend.domain.usage.model.ApiUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiUsageLogMapper {

    int insert(ApiUsageLog usageLog);

    int countByUserAndFeature(@Param("userId") Long userId, @Param("feature") String feature);
}
