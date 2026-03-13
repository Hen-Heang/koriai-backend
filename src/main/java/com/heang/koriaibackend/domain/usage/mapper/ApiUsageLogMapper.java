package com.heang.koriaibackend.domain.usage.mapper;

import com.heang.koriaibackend.domain.usage.model.ApiUsageLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiUsageLogMapper {

    int insert(ApiUsageLog usageLog);
}
