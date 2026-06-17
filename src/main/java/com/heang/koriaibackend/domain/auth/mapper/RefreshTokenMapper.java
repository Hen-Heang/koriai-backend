package com.heang.koriaibackend.domain.auth.mapper;

import com.heang.koriaibackend.domain.auth.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    /** Revokes a single token by hash (no-op if already revoked). Returns rows affected. */
    int revokeByTokenHash(@Param("tokenHash") String tokenHash);

    /** Revokes every active token for a user ("log out of all devices"). */
    int revokeAllByUserId(@Param("userId") Long userId);

    /** Housekeeping: removes expired or revoked rows. */
    int deleteExpired();
}
