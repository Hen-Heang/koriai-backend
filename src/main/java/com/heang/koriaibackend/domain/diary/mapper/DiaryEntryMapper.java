package com.heang.koriaibackend.domain.diary.mapper;

import com.heang.koriaibackend.domain.diary.model.DiaryEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DiaryEntryMapper {

    int upsert(DiaryEntry diaryEntry);

    List<DiaryEntry> findByUserIdAndMonth(@Param("userId") Long userId,
                                          @Param("monthStart") LocalDate monthStart,
                                          @Param("monthEndExclusive") LocalDate monthEndExclusive);
}
