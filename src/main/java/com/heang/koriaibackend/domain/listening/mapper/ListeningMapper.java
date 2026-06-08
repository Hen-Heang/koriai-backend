package com.heang.koriaibackend.domain.listening.mapper;

import com.heang.koriaibackend.domain.listening.model.ListeningAttempt;
import com.heang.koriaibackend.domain.listening.model.ListeningLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ListeningMapper {
    int insertLesson(ListeningLesson lesson);

    ListeningLesson findLessonByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    List<ListeningLesson> findLessonsByUserId(@Param("userId") Long userId);

    int insertAttempt(ListeningAttempt attempt);

    int countCompletedAttempts(@Param("userId") Long userId);
}
