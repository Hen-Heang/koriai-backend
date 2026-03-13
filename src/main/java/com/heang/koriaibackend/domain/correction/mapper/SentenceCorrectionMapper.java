package com.heang.koriaibackend.domain.correction.mapper;

import com.heang.koriaibackend.domain.correction.model.SentenceCorrection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SentenceCorrectionMapper {

    int insert(SentenceCorrection correction);

    List<SentenceCorrection> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
