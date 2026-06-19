package com.heang.koriaibackend.domain.correction.mapper;

import com.heang.koriaibackend.domain.correction.model.SentenceCorrection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SentenceCorrectionMapper {

    int insert(SentenceCorrection correction);

    List<SentenceCorrection> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    List<SentenceCorrection> findDueByUserId(@Param("userId") Long userId);

    int countReviewed(@Param("userId") Long userId);

    SentenceCorrection findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int updateSrs(SentenceCorrection correction);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
