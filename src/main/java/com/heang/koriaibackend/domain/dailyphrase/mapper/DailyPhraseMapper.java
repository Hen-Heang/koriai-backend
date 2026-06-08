package com.heang.koriaibackend.domain.dailyphrase.mapper;

import com.heang.koriaibackend.domain.dailyphrase.model.DailyPhrase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyPhraseMapper {
    int insert(DailyPhrase phrase);

    DailyPhrase findByUserAndDate(@Param("userId") Long userId, @Param("phraseDate") LocalDate phraseDate);

    DailyPhrase findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    List<DailyPhrase> findByUserId(@Param("userId") Long userId);

    List<String> findRecentPhrases(@Param("userId") Long userId, @Param("limit") int limit);

    int updateLearned(@Param("id") Long id, @Param("userId") Long userId, @Param("learned") boolean learned);
}
