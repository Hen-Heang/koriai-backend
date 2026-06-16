package com.heang.koriaibackend.domain.push.mapper;

import com.heang.koriaibackend.domain.push.model.TelegramLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TelegramLinkMapper {

    /**
     * Store (or replace) a pending link code for the user, leaving any existing
     * confirmed chat untouched until the new code is redeemed.
     */
    int upsertLinkCode(@Param("userId") Long userId, @Param("linkCode") String linkCode);

    TelegramLink findByUserId(@Param("userId") Long userId);

    TelegramLink findByLinkCode(@Param("linkCode") String linkCode);

    /** Bind the resolved chat to the user and clear the one-time link code. */
    int confirmChat(@Param("userId") Long userId, @Param("chatId") Long chatId);

    int deleteByUserId(@Param("userId") Long userId);
}
