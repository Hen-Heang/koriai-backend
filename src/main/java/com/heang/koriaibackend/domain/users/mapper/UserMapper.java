package com.heang.koriaibackend.domain.users.mapper;

import com.heang.koriaibackend.domain.users.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    int insert(User user);

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByEmail(@Param("email") String email);

    /** Search users by display name or email (excluding the caller), for invitations. */
    List<User> searchByQuery(@Param("query") String query,
                             @Param("excludeId") Long excludeId,
                             @Param("limit") int limit);

    int updateProfile(@Param("id") Long id,
                      @Param("displayName") String displayName,
                      @Param("koreanLevel") String koreanLevel);

    int updatePreferredModel(@Param("id") Long id, @Param("preferredModel") String preferredModel);

    int updateStudyReminders(@Param("id") Long id,
                             @Param("enabled") boolean enabled,
                             @Param("hour") int hour);

    int updateProfileImage(@Param("id") Long id,
                           @Param("contentType") String contentType,
                           @Param("data") byte[] data);

    /** Loads only the image bytes + content type (heavy column kept out of normal lookups). */
    Optional<User> findProfileImage(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
