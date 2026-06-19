package com.heang.koriaibackend.domain.note.mapper;

import com.heang.koriaibackend.domain.note.model.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {
    int insertNote(Note note);

    int updateNote(Note note);

    int deleteByUserAndSlug(@Param("userId") Long userId, @Param("slug") String slug);

    Note findByUserAndSlug(@Param("userId") Long userId, @Param("slug") String slug);

    // Metadata only (no content) for the library/index page.
    List<Note> findMetaByUserId(@Param("userId") Long userId);
}
