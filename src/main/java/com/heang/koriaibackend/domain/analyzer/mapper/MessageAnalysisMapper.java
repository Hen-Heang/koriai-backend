package com.heang.koriaibackend.domain.analyzer.mapper;

import com.heang.koriaibackend.domain.analyzer.model.MessageAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageAnalysisMapper {

    int insert(MessageAnalysis analysis);

    List<MessageAnalysis> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
