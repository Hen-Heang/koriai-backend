package com.heang.koriaibackend.domain.foundation.mapper;

import com.heang.koriaibackend.domain.foundation.model.FoundationProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoundationProgressMapper {
    List<FoundationProgress> findByUser(@Param("userId") Long userId);

    FoundationProgress findByUserAndLesson(@Param("userId") Long userId, @Param("lessonId") String lessonId);

    // Upsert: keeps the best accuracy, makes completion sticky, and bumps the
    // attempt counter on every call.
    int upsert(FoundationProgress progress);
}
