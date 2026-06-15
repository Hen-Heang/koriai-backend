package com.heang.koriaibackend.domain.interview.mapper;

import com.heang.koriaibackend.domain.interview.model.InterviewScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InterviewScriptMapper {

    InterviewScript findByUserAndTopic(@Param("userId") Long userId, @Param("topicId") String topicId);

    int upsert(InterviewScript script);
}
